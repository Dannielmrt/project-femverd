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
from app.services.security_service import hash_dni
from app.services.audit_service import send_audit_log
from app.services.adapters.ecopark_v1 import EcoparkAdapter

router = APIRouter(prefix="/ingestion", tags=["Ingest M2M (External)"])

# Registry pattern for scalable M2M protocol adapters
ADAPTER_REGISTRY = {
    "ecopark_v1": EcoparkAdapter,
}

def get_adapter(adapter_type: str):
    adapter_class = ADAPTER_REGISTRY.get(adapter_type.lower())
    if not adapter_class:
        raise HTTPException(status_code=400, detail=f"No adapter registered for type: {adapter_type}")
    return adapter_class()

def process_event_in_background(user_id: int, rule_id: int, green_point_id: int, quantity: float, provider_id: str):
    """
    Delegated worker function executing outside the main request and response
    """
    db = SessionLocal()
    try:
        rule = db.query(MaterialRule).filter(MaterialRule.id == rule_id).first()
        points_earned = points_service.calculate_points(rule.points_per_unit, quantity)
        
        # Atomic points update to prevent database race conditions under high concurrency
        db.query(User).filter(User.id == user_id).update({
            "points_balance": User.points_balance + points_earned,
            "total_accumulated_points": User.total_accumulated_points + points_earned
        })

        new_action = Action(
            user_id=user_id, 
            quantity=quantity,
            generated_points=points_earned,
            green_point_id=green_point_id,
            material_rule_id=rule_id
        )
        
        db.add(new_action)
        db.commit()
        
        print(f"BACKGROUND I/O SUCCESS: {points_earned} points provisioned.", flush=True)
        send_audit_log(f"User ID {user_id} recycled {quantity}kg at {provider_id}")
        
    except Exception as e:
        db.rollback()
        print(f"BACKGROUND I/O CRITICAL ERROR: {str(e)}", flush=True)
    finally:
        db.close()

@router.post("/{provider_id}", status_code=status.HTTP_202_ACCEPTED)
def receive_event(
    provider_id: str, 
    raw_payload: Dict[str, Any], 
    background_tasks: BackgroundTasks, 
    api_key: str = Depends(get_api_key)
):
    db = SessionLocal()
    try:
        provider = db.query(ExternalSystem).filter(ExternalSystem.provider_id == provider_id).first()
        if not provider or not bcrypt.checkpw(api_key.encode('utf-8'), provider.api_key_hash.encode('utf-8')):
            raise HTTPException(status_code=403, detail="Invalid API Key or Provider credentials")

        adapter = get_adapter(provider.adapter_type)
        event = adapter.normalize(raw_payload)

        # Constant look via hashing
        search_hash = hash_dni(event.user_dni)
        user = db.query(User).filter(User.dni_hash == search_hash).first()
        if not user:
            raise HTTPException(status_code=404, detail="Target user identity not found")

        rule = db.query(MaterialRule).filter(MaterialRule.material_name == event.material_type).first()
        green_point = db.query(GreenPoint).filter(GreenPoint.provider_id == provider_id).first()
        
        if not rule or not green_point:
            raise HTTPException(status_code=400, detail="Business logic rule or structural entity missing")

        # put operations to the background queue
        background_tasks.add_task(
            process_event_in_background, 
            user.id, rule.id, green_point.id, event.quantity, provider_id
        )

        return {"status": "Accepted", "message": "Event queued for asynchronous processing"}
    finally:
        db.close()