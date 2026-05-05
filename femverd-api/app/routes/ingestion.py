from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import Dict, Any
from app.auth.security import verify_external_role
from ..services import points_service
from ..database import get_db
from ..models.user import User
from ..models.action import Action
from app.services.security_service import decrypt_dni, encrypt_dni

# Import our adapters
from app.services.adapters.ecopark_v1 import EcoparkAdapter

router = APIRouter(prefix="/ingestion", tags=["Ingest M2M (External)"])

def get_adapter(provider_name: str):
    """Factory to return the correct adapter based on the provider"""
    if provider_name.lower() == "ecopark":
        return EcoparkAdapter()
    # elif provider_name.lower() == "supermarket":
    #     return SupermarketAdapter()
    
    raise HTTPException(status_code=400, detail=f"No adapter found for provider: {provider_name}")


# dynamic {provider_name} in the URL
@router.post("/{provider_name}", dependencies=[Depends(verify_external_role)])
def receive_event(provider_name: str, raw_payload: Dict[str, Any], db: Session = Depends(get_db)):
    
    # Get the right adapter and normalize the messy JSON
    adapter = get_adapter(provider_name)
    try:
        event = adapter.normalize(raw_payload)
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Error parsing external data: {str(e)}")

    # Search users and decrypt to compare
    all_users = db.query(User).all()
    user = None
    
    for u in all_users:
        if decrypt_dni(u.encrypted_dni) == event.user_dni:
            user = u
            break
    
    if not user:
        raise HTTPException(status_code=404, detail="User not found in FemVerd")

    # Calculate points using our Service
    points_earned = points_service.calculate_points(
        material=event.material_type, 
        kg=event.amount_kg
    )

    # Update user balance
    user.points_balance += points_earned

    # Create the action record
    new_action = Action(
        user_dni=encrypt_dni(event.user_dni), 
        provider_id=event.provider_id,
        material_type=event.material_type,
        amount_kg=event.amount_kg,
        generated_points=points_earned
    )
    
    # Save everything to the database
    db.add(new_action)
    db.commit()

    return {
        "status": "Accepted",
        "user": user.user_name,
        "points_earned": points_earned,
        "new_total_balance": user.points_balance
    }