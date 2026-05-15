# app/routes/auth.py
from fastapi import APIRouter, Depends, HTTPException, status, Query
from fastapi.security import OAuth2PasswordRequestForm
from sqlalchemy.orm import Session
from typing import Optional
import bcrypt

from app.database import get_db
from app.models.user import User
from app.models.action import Action
from app.models.material_rule import MaterialRule
from app.services.security_service import hash_dni
from app.services.auth_service import create_access_token
from app.auth.security import get_current_user_token

router = APIRouter(prefix="/auth", tags=["Mobile App Authentication"])

@router.post("/login")
def login_for_access_token(form_data: OAuth2PasswordRequestForm = Depends(), db: Session = Depends(get_db)):
    """
    Authenticates the mobile app user using the Blind Index pattern for the DNI
    and Bcrypt for the password. Returns an RSA-signed JWT.
    """
    # Search user instantly via Blind Index (Hash)
    search_hash = hash_dni(form_data.username)
    user = db.query(User).filter(User.dni_hash == search_hash).first()

    if not user or not user.hashed_password:
        raise HTTPException(status_code=401, detail="Incorrect credentials")

    # Verify password securely using Bcrypt
    if not bcrypt.checkpw(form_data.password.encode('utf-8'), user.hashed_password.encode('utf-8')):
        raise HTTPException(status_code=401, detail="Incorrect password")
        
    # Create the JWT Token with the raw DNI as the subject ("sub")
    access_token = create_access_token(data={"sub": form_data.username})
    return {"access_token": access_token, "token_type": "bearer"}

@router.get("/me")
def read_users_me(user_dni: str = Depends(get_current_user_token), db: Session = Depends(get_db)):
    """
    Protected route returning the current user's profile and points balance.
    """
    search_hash = hash_dni(user_dni)
    user = db.query(User).filter(User.dni_hash == search_hash).first()
    
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    return {
        "dni": user_dni,
        "name": user.user_name,
        "current_points": user.points_balance,
        "total_points": user.total_accumulated_points
    }

@router.delete("/me")
def delete_user_account(user_dni: str = Depends(get_current_user_token), db: Session = Depends(get_db)):
    """
    Completely removes the user and their associated data (Right to be forgotten/RGPD).
    """
    search_hash = hash_dni(user_dni)
    user = db.query(User).filter(User.dni_hash == search_hash).first()

    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    # Cascade Delete using SQL directly 
    db.query(Action).filter(Action.user_id == user.id).delete()
    
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
    """
    Returns the user's recycling history using highly optimized indexed DB queries.
    """
    # Find user via Blind Index
    search_hash = hash_dni(user_dni)
    user = db.query(User).filter(User.dni_hash == search_hash).first()
    
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    # Direct SQL query on actions filtered by user_id
    query = db.query(Action).filter(Action.user_id == user.id)

    # Apply optional filters at the SQL level
    if min_quantity:
        query = query.filter(Action.quantity >= min_quantity)
        
    if material:
        query = query.join(MaterialRule).filter(MaterialRule.material_name == material)

    # Sort by newest and apply the result limit
    actions = query.order_by(Action.id.desc()).limit(limit).all()

    return [
        {
            "id": a.id,
            "date": a.created_at.isoformat() if a.created_at else None,
            "quantity": a.quantity,
            "generated_points": a.generated_points,
            "material_id": a.material_rule_id,
            "green_point_id": a.green_point_id
        } for a in actions
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
    search_hash = hash_dni(user_dni)
    user = db.query(User).filter(User.dni_hash == search_hash).first()
    
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    # Fetch only the actions corresponding to the requested year using SQL functions
    from sqlalchemy import extract
    actions = db.query(Action).filter(
        Action.user_id == user.id,
        extract('year', Action.created_at) == year
    ).all()

    # Group and aggregate data by material
    materials_summary = {}
    total_points = 0.0

    for a in actions:
        rule = db.query(MaterialRule).filter(MaterialRule.id == a.material_rule_id).first()
        mat_name = rule.material_name if rule else "Unknown"
        unit = rule.unit_type if rule else "ud"

        if mat_name not in materials_summary:
            materials_summary[mat_name] = {"total_quantity": 0.0, "unit": unit}
            
        materials_summary[mat_name]["total_quantity"] += a.quantity
        total_points += a.generated_points

    # Format the aggregated output for the mobile app
    return {
        "certificate_year": year,
        "citizen_name": user.user_name,
        "citizen_dni": user_dni, # Return raw DNI for the official document
        "member_since": user.created_at.strftime("%Y-%m-%d") if user.created_at else None,
        "total_points_generated": total_points,
        "materials_breakdown": [
            {"material": k, "total_quantity": v["total_quantity"], "unit": v["unit"]}
            for k, v in materials_summary.items()
        ]
    }