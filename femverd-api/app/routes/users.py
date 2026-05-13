# app/routes/users.py
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
import bcrypt

from ..database import get_db
from ..models.user import User
from ..schemas.user_schema import UserCreate
from app.services.security_service import encrypt_dni

router = APIRouter(prefix="/users", tags=["Users"])

@router.post("/")
def create_user(user_data: UserCreate, db: Session = Depends(get_db)):
    
    # Encrypt DNI with Fernet 
    secure_dni = encrypt_dni(user_data.dni)
    
    # Hash Password with Bcrypt 
    salt = bcrypt.gensalt()
    hashed_pw = bcrypt.hashpw(user_data.password.encode('utf-8'), salt).decode('utf-8')
    
    # Save user with both layers of security
    new_user = User(
        user_name=user_data.user_name, 
        email=user_data.email,
        encrypted_dni=secure_dni,
        hashed_password=hashed_pw  # Save the hash
    )
    
    db.add(new_user)
    db.commit()            
    db.refresh(new_user)   
    
    return new_user