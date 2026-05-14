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

def process_event_in_background(user_id: int, rule_id: int, green_point_id: int, quantity: float, user_dni_raw: str, material_type: str, provider_id: str):
    """
    BACKGROUND THREAD: receives IDs directly to avoid searching again and prevent decryption loops in background.
    """
    db = SessionLocal()
    try:
        # Atomic points update
        points_earned = points_service.calculate_points(
            db.query(MaterialRule).filter(MaterialRule.id == rule_id).first().points_per_unit, 
            quantity
        )
        
        db.query(User).filter(User.id == user_id).update(
            {"points_balance": User.points_balance + points_earned}
        )

        # Save Action
        new_action = Action(
            user_dni=encrypt_dni(user_dni_raw),
            quantity=quantity,
            generated_points=points_earned,
            green_point_id=green_point_id,
            material_rule_id=rule_id
        )
        
        db.add(new_action)
        db.commit()
        
        # Logs
        print(f"BACKGROUND SUCCESS: {points_earned} points added.", flush=True)
        send_audit_log(f"User ID {user_id} recycled {quantity}kg at {provider_id}")
        
    except Exception as e:
        db.rollback()
        print(f"BACKGROUND CRITICAL ERROR: {str(e)}", flush=True)
    finally:
        db.close() # Always close the connection to avoid hanging the reload


@router.post("/{provider_id}", status_code=status.HTTP_202_ACCEPTED)
def receive_event(
    provider_id: str, 
    raw_payload: Dict[str, Any], 
    background_tasks: BackgroundTasks, 
    api_key: str = Depends(get_api_key)
):
    db = SessionLocal()
    try:
        # Provider validation
        provider = db.query(ExternalSystem).filter(ExternalSystem.provider_id == provider_id).first()
        if not provider:
            raise HTTPException(status_code=404, detail="Provider not found")

        if not bcrypt.checkpw(api_key.encode('utf-8'), provider.api_key_hash.encode('utf-8')):
            raise HTTPException(status_code=403, detail="Invalid API Key")

        # Adapter & Normalization
        adapter = get_adapter(provider.adapter_type)
        event = adapter.normalize(raw_payload)

        # Check if Rule and User exist BEFORE starting background task
        rule = db.query(MaterialRule).filter(MaterialRule.material_name == event.material_type).first()
        if not rule:
            raise HTTPException(status_code=400, detail=f"Material rule '{event.material_type}' not found")

        all_users = db.query(User).all()
        user = next((u for u in all_users if decrypt_dni(u.encrypted_dni) == event.user_dni), None)
        if not user:
            raise HTTPException(status_code=404, detail=f"User with DNI {event.user_dni} not found")

        green_point = db.query(GreenPoint).filter(GreenPoint.provider_id == provider_id).first()
        if not green_point:
            raise HTTPException(status_code=404, detail="Green point not linked to provider")

        # Launch background task with ALREADY VALIDATED data
        background_tasks.add_task(
            process_event_in_background, 
            user.id, rule.id, green_point.id, event.quantity, event.user_dni, event.material_type, provider_id
        )

        return {"status": "Accepted", "message": "Processing recycling event..."}

    finally:
        db.close()