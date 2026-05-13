# app/routes/ingestion.py
from fastapi import APIRouter, Depends, HTTPException, BackgroundTasks, status
from typing import Dict, Any
import bcrypt

from app.auth.security import get_api_key
from app.services import points_service
from app.database import SessionLocal
from app.models.user import User
from app.models.action import Action
from app.models.external_system import ExternalSystem
from app.models.green_point import GreenPoint
from app.models.material_rule import MaterialRule
from app.services.security_service import decrypt_dni, encrypt_dni
from app.services.audit_service import send_audit_log

# Import adapters
from app.services.adapters.ecopark_v1 import EcoparkAdapter

router = APIRouter(prefix="/ingestion", tags=["Ingest M2M (External)"])

ADAPTER_REGISTRY = {
    "ecopark_v1": EcoparkAdapter,
    # "smartbin_v1": SmartBinAdapter 
}

def get_adapter(adapter_type: str):
    """
    Dynamic factory using Registry to return the correct adapter based on the DB type
    """
    adapter_class = ADAPTER_REGISTRY.get(adapter_type.lower())
    
    if not adapter_class:
        raise HTTPException(
            status_code=400, 
            detail=f"No adapter registered for type: {adapter_type}. Available: {list(ADAPTER_REGISTRY.keys())}"
        )
    
    return adapter_class()

def process_event_in_background(event, provider_id: str):
    """
    BACKGROUND THREAD: Handles the heavy DB operations asynchronously.
    """
    # Open an independent DB session for this thread
    db = SessionLocal()
    
    try:
        # Fetch relations
        green_point = db.query(GreenPoint).filter(GreenPoint.provider_id == provider_id).first()
        rule = db.query(MaterialRule).filter(MaterialRule.material_name == event.material_type).first()
        
        # Find User (Fernet)
        all_users = db.query(User).all()
        user = next((u for u in all_users if decrypt_dni(u.encrypted_dni) == event.user_dni), None)
        
        if not user or not rule or not green_point:
            # print to logs instead of raising HTTP exceptions
            print(f"BACKGROUND ERROR: Missing data for DNI {event.user_dni} or Rule {event.material_type}", flush=True)
            return

        # Calculate and Update atomically and prevent race conditions in DB
        points_earned = points_service.calculate_points(rule.points_per_unit, event.quantity)
        
        # add the points directly avoiding read-modify-write issues
        db.query(User).filter(User.id == user.id).update(
            {"points_balance": User.points_balance + points_earned}
        )

        # Save Action
        new_action = Action(
            user_dni=encrypt_dni(event.user_dni),
            quantity=event.quantity,
            generated_points=points_earned,
            green_point_id=green_point.id,
            material_rule_id=rule.id
        )
        
        db.add(new_action)
        db.commit()
        
        print(f"BACKGROUND SUCCESS: {points_earned} points added to {user.user_name}", flush=True)

        # Send secure log with tcp sockets
        log_msg = f"User {user.user_name} (DNI Hash: {new_action.user_dni[-10:]}) recycled {event.quantity}kg of {event.material_type} at {provider_id}."
        send_audit_log(log_msg)
        
    except Exception as e:
        print(f"BACKGROUND CRITICAL ERROR: {str(e)}", flush=True)
        db.rollback()
    finally:
        db.close()


# status_code=202
@router.post("/{provider_id}", status_code=status.HTTP_202_ACCEPTED)
def receive_event(
    provider_id: str, 
    raw_payload: Dict[str, Any], 
    background_tasks: BackgroundTasks, 
    api_key: str = Depends(get_api_key)
):
    db = SessionLocal()
    try:
        # Search por the provider in the DB using URL 
        provider = db.query(ExternalSystem).filter(ExternalSystem.provider_id == provider_id).first()
        if not provider:
            raise HTTPException(status_code=404, detail="Provider not found in DB")

        # Check the API Key
        if not bcrypt.checkpw(api_key.encode('utf-8'), provider.api_key_hash.encode('utf-8')):
            raise HTTPException(status_code=403, detail="Invalid Provider or API Key")

        # Get the adapter from the DB
        adapter = get_adapter(provider.adapter_type)
        
        # Translate to JSON
        event = adapter.normalize(raw_payload)
        
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Data parsing error: {str(e)}")
    finally:
        db.close()

    # Launch background thread 
    background_tasks.add_task(process_event_in_background, event, provider.provider_id)

    # response to the external system
    return {
        "status": "Accepted",
        "message": "Event received and currently processing in the background."
    }