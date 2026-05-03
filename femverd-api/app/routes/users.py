from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from ..database import get_db
from ..models.user import User
from ..schemas.user_schema import UserCreate

# Creamos un "mini-FastAPI" solo para usuarios
router = APIRouter(prefix="/users", tags=["Usuarios"])

@router.post("/")
def create_user(user_data: UserCreate, db: Session = Depends(get_db)):
    # 1. Por ahora guardamos el DNI tal cual (más adelante aplicarás tu AES)
    nuevo_usuario = User(
        nombre=user_data.nombre, 
        dni_cifrado=user_data.dni 
    )
    
    # 2. Lo añadimos a la base de datos
    db.add(nuevo_usuario)
    db.commit()               # Confirmamos los cambios
    db.refresh(nuevo_usuario) # Obtenemos el ID que le ha dado PostgreSQL
    
    return nuevo_usuario