from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from pydantic import BaseModel
from app.auth.security import verify_external_role
from ..services import points_service
from ..database import get_db
from ..models.user import User
from ..models.action import Action
from app.services.security_service import decrypt_dni, encrypt_dni

router = APIRouter(prefix="/ingestion", tags=["Ingest M2M (External)"])

class ExternalEvent(BaseModel):
    provider_id: str
    user_dni: str
    material_type: str
    amount_kg: float

@router.post("/", dependencies=[Depends(verify_external_role)])
def receive_event(event: ExternalEvent, db: Session = Depends(get_db)):
    # Search users and decrypt to compare
    all_users = db.query(User).all()
    user = None
    
    for u in all_users:
        if decrypt_dni(u.encrypted_dni) == event.user_dni:
            user = u
            break
    
    if not user:
        # If DNI is not registered, raise a 404 error
        raise HTTPException(status_code=404, detail="User not found in FemVerd")

    # Calculate points using our Service
    points_earned = points_service.calculate_points(
        material=event.material_type, 
        kg=event.amount_kg
    )

    # Update user balance
    user.points_balance += points_earned

    # Create the "ticket" or action record
    # Encrypt the DNI so it remains protected in the logs
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