# app/routes/auth.py
from fastapi import APIRouter, Depends, HTTPException, status, Query
from fastapi.security import OAuth2PasswordRequestForm
from sqlalchemy.orm import Session
from typing import Optional, List
from datetime import datetime
import bcrypt

from app.database import get_db
from app.models.user import User
from app.models.action import Action
from app.models.material_rule import MaterialRule
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
    
    if not user.hashed_password:
        raise HTTPException(status_code=401, detail="Legacy user without password. Please register again.")

    # Verify password securely using Bcrypt
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

@router.delete("/me")
def delete_user_account(
    user_dni: str = Depends(get_current_user_token),
    db: Session = Depends(get_db)
):
    """
    Deletes the user account and associated recycling records.
    """
    all_users = db.query(User).all()
    user = next((u for u in all_users if decrypt_dni(u.encrypted_dni) == user_dni), None)

    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    # Cascade Delete, remove associated actions to prevent orphaned records
    all_actions = db.query(Action).all()
    actions_to_delete = [a for a in all_actions if decrypt_dni(a.user_dni) == user_dni]
    
    for action in actions_to_delete:
        db.delete(action)

    # finally delete the user
    db.delete(user)
    db.commit()

    return {"message": "Account and associated data successfully deleted"}

@router.get("/me/history")
def get_user_history(
    material: Optional[str] = Query(None, description="Filter by material name"),
    min_quantity: Optional[float] = Query(None, description="Filter by minimum quantity"),
    limit: int = Query(10, description="Maximum number of records to return"),
    user_dni: str = Depends(get_current_user_token),
    db: Session = Depends(get_db)
):
    all_actions = db.query(Action).all()
    user_actions = [a for a in all_actions if decrypt_dni(a.user_dni) == user_dni]

    filtered_actions = []
    for action in user_actions:
        if min_quantity and action.quantity < min_quantity:
            continue
            
        if material:
            rule = db.query(MaterialRule).filter(MaterialRule.id == action.material_rule_id).first()
            if not rule or rule.material_name.lower() != material.lower():
                continue
                
        filtered_actions.append(action)

    filtered_actions.sort(key=lambda x: x.id, reverse=True)
    result = filtered_actions[:limit]

    return [
        {
            "id": a.id,
            "date": a.created_at.isoformat() if a.created_at else None,
            "quantity": a.quantity,
            "generated_points": a.generated_points,
            "material_id": a.material_rule_id,
            "green_point_id": a.green_point_id
        } for a in result
    ]

@router.get("/me/certificate")
def get_annual_certificate(
    year: int = Query(..., description="Year for the tax reduction certificate"),
    user_dni: str = Depends(get_current_user_token),
    db: Session = Depends(get_db)
):
    """
    Generates the aggregated data needed for the Official Environmental Impact Certificate.
    """
    # Search user
    all_users = db.query(User).all()
    user = next((u for u in all_users if decrypt_dni(u.encrypted_dni) == user_dni), None)
    
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    # Filter by actions and year
    all_actions = db.query(Action).all()
    user_actions = [
        a for a in all_actions 
        if decrypt_dni(a.user_dni) == user_dni and a.created_at and a.created_at.year == year
    ]

    # Group data by material
    materials_summary = {}
    total_points_year = 0.0

    for action in user_actions:
        rule = db.query(MaterialRule).filter(MaterialRule.id == action.material_rule_id).first()
        mat_name = rule.material_name if rule else "Desconocido"
        unit = rule.unit_type if rule else "ud"

        if mat_name not in materials_summary:
            materials_summary[mat_name] = {"total_quantity": 0.0, "unit": unit}
            
        materials_summary[mat_name]["total_quantity"] += action.quantity
        total_points_year += action.generated_points

    # Format the output for the app
    formatted_materials = [
        {"material": name, "total_quantity": data["total_quantity"], "unit": data["unit"]}
        for name, data in materials_summary.items()
    ]

    return {
        "certificate_year": year,
        "citizen_name": user.user_name,
        "citizen_dni": user_dni, # Decrypted DNI (for the official document only)
        "member_since": user.created_at.strftime("%Y-%m-%d") if user.created_at else None,
        "total_points_generated": total_points_year,
        "materials_breakdown": formatted_materials
    }