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

# Import adapters
from app.services.adapters.ecopark_v1 import EcoparkAdapter

router = APIRouter(prefix="/ingestion", tags=["Ingest M2M (External)"])

def get_adapter(provider_name: str):
    """Factory to return the correct adapter based on the provider."""
    if provider_name.lower() == "ecopark":
        return EcoparkAdapter()
    raise HTTPException(status_code=400, detail=f"No adapter found for provider: {provider_name}")

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

        # Calculate and Update
        points_earned = points_service.calculate_points(rule.points_per_unit, event.amount_kg)
        user.points_balance += points_earned

        # Save Action
        new_action = Action(
            user_dni=encrypt_dni(event.user_dni),
            amount_kg=event.amount_kg,
            generated_points=points_earned,
            green_point_id=green_point.id,
            material_rule_id=rule.id
        )
        
        db.add(new_action)
        db.commit()
        
        print(f"BACKGROUND SUCCESS: {points_earned} points added to {user.user_name}", flush=True)
        
    except Exception as e:
        print(f"BACKGROUND CRITICAL ERROR: {str(e)}", flush=True)
        db.rollback()
    finally:
        db.close()


# status_code=202
@router.post("/{provider_name}", status_code=status.HTTP_202_ACCEPTED)
def receive_event(
    provider_name: str, 
    raw_payload: Dict[str, Any], 
    background_tasks: BackgroundTasks, 
    api_key: str = Depends(get_api_key)
):
    
    # Translate JSON
    adapter = get_adapter(provider_name)
    try:
        event = adapter.normalize(raw_payload)
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Data parsing error: {str(e)}")

    # Security Check (API Key)
    db = SessionLocal()
    try:
        provider = db.query(ExternalSystem).filter(ExternalSystem.provider_id == event.provider_id).first()
        if not provider or not bcrypt.checkpw(api_key.encode('utf-8'), provider.api_key_hash.encode('utf-8')):
            raise HTTPException(status_code=403, detail="Invalid Provider or API Key")
    finally:
        db.close()

    background_tasks.add_task(process_event_in_background, event, provider.provider_id)

    # response to the external system
    return {
        "status": "Accepted",
        "message": "Event received and currently processing in the background."
    }