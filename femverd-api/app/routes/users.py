from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from ..database import get_db
from ..models.user import User
from ..schemas.user_schema import UserCreate
from app.services.security_service import encrypt_dni

router = APIRouter(prefix="/users", tags=["Users"])

@router.post("/")
def create_user(user_data: UserCreate, db: Session = Depends(get_db)):
    
    # Pass the raw DNI through our encryption service (AES Encryption)
    secure_dni = encrypt_dni(user_data.dni)
    
    # Save user with the DNI converted into ciphered text
    new_user = User(
        user_name=user_data.user_name, 
        encrypted_dni=secure_dni
    )
    
    # Add to database and persist changes
    db.add(new_user)
    db.commit()            # Commit changes
    db.refresh(new_user)   # Retrieve the generated ID from PostgreSQL
    
    return new_user