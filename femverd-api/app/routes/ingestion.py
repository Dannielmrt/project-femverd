# app/routes/ingestion.py
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import Dict, Any
import bcrypt

from app.auth.security import get_api_key
from app.services import points_service
from app.database import get_db
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

@router.post("/{provider_name}")
def receive_event(
    provider_name: str, 
    raw_payload: Dict[str, Any], 
    api_key: str = Depends(get_api_key), 
    db: Session = Depends(get_db)
):
    
    # Translate external JSON to NormalizedEvent
    adapter = get_adapter(provider_name)
    try:
        event = adapter.normalize(raw_payload)
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Data parsing error: {str(e)}")

    # Security: Verify Provider and API Key (Bcrypt)
    provider = db.query(ExternalSystem).filter(ExternalSystem.provider_id == event.provider_id).first()
    
    if not provider or not bcrypt.checkpw(api_key.encode('utf-8'), provider.api_key_hash.encode('utf-8')):
        raise HTTPException(status_code=403, detail="Invalid Provider or API Key")

    # Find Green Point (For simplicity, we take the first one linked to the provider)
    green_point = db.query(GreenPoint).filter(GreenPoint.provider_id == provider.provider_id).first()
    if not green_point:
        raise HTTPException(status_code=404, detail="No Green Point registered for this provider")

    # Find Material Rule
    rule = db.query(MaterialRule).filter(MaterialRule.material_name == event.material_type).first()
    if not rule:
        raise HTTPException(status_code=404, detail=f"Material '{event.material_type}' is not supported")

    # Find User (handling Fernet non-deterministic encryption)
    all_users = db.query(User).all()
    user = None
    for u in all_users:
        if decrypt_dni(u.encrypted_dni) == event.user_dni:
            user = u
            break
            
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    # Calculate Points
    points_earned = points_service.calculate_points(
        points_per_unit=rule.points_per_unit, 
        amount=event.amount_kg
    )

    # Update User Balance
    user.points_balance += points_earned

    # Create Action Record (Using Foreign Keys)
    new_action = Action(
        user_dni=encrypt_dni(event.user_dni),
        amount_kg=event.amount_kg,
        generated_points=points_earned,
        green_point_id=green_point.id,  # Foreign Key
        material_rule_id=rule.id  # Foreign Key
    )
    
    db.add(new_action)
    db.commit()

    return {
        "status": "Accepted",
        "user": user.user_name,
        "material": rule.material_name,
        "points_earned": points_earned,
        "new_total_balance": user.points_balance
    }