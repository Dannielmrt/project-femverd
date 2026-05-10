# app/routes/auth.py
from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordRequestForm
from sqlalchemy.orm import Session
import bcrypt

from app.database import get_db
from app.models.user import User
from app.services.security_service import decrypt_dni
from app.services.auth_service import create_access_token
from app.auth.security import get_current_user_token

router = APIRouter(prefix="/auth", tags=["Mobile App Authentication"])

@router.post("/login")
def login_for_access_token(
    form_data: OAuth2PasswordRequestForm = Depends(), 
    db: Session = Depends(get_db)
):
    """
    Receives DNI (username) and password from the mobile app.
    Returns an RSA-signed JWT if credentials are correct.
    """
    # Search for the user by DNI (handling Fernet encryption)
    all_users = db.query(User).all()
    user = next((u for u in all_users if decrypt_dni(u.encrypted_dni) == form_data.username), None)

    if not user:
        raise HTTPException(status_code=401, detail="Incorrect DNI")

    # 2. Verify password securely using Bcrypt
    if not bcrypt.checkpw(form_data.password.encode('utf-8'), user.hashed_password.encode('utf-8')):
        raise HTTPException(status_code=401, detail="Incorrect password")

    # Create the JWT Token with the DNI as the subject ("sub")
    access_token = create_access_token(data={"sub": form_data.username})
    
    return {"access_token": access_token, "token_type": "bearer"}

@router.get("/me")
def read_users_me(
    user_dni: str = Depends(get_current_user_token), 
    db: Session = Depends(get_db)
):
    """
    Protected route. Requires a valid JWT in the Authorization header.
    Returns the user's current points balance.
    """
    all_users = db.query(User).all()
    user = next((u for u in all_users if decrypt_dni(u.encrypted_dni) == user_dni), None)
    
    if not user:
        raise HTTPException(status_code=404, detail="User not found in DB")

    return {
        "dni": user_dni,
        "name": user.user_name,
        "current_points": user.points_balance
    }