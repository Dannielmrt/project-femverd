from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from pydantic import BaseModel
from ..services import points_service
from ..database import get_db
from ..models.user import User
from ..models.action import Action

router = APIRouter(prefix="/ingestion", tags=["Ingesta M2M (Externos)"])

class ExternalEvent(BaseModel):
    provider_id: str
    dni_usuario: str
    tipo_material: str
    cantidad_kg: float

@router.post("/")
def receive_event(evento: ExternalEvent, db: Session = Depends(get_db)):
    
    # 1. Comprobamos si el usuario existe en nuestra DB
    usuario = db.query(User).filter(User.dni_cifrado == evento.dni_usuario).first()
    
    if not usuario:
        # Si el DNI no está registrado, lanzamos un error 404
        raise HTTPException(status_code=404, detail="Usuario no encontrado en FemVerd")

    # 2. Calculamos los puntos con nuestro Service
    puntos_ganados = points_service.calculate_points(
        material=evento.tipo_material, 
        kg=evento.cantidad_kg
    )

    # 3. Actualizamos el saldo del usuario
    usuario.saldo_puntos += puntos_ganados

    # 4. Creamos el "ticket" o registro de la acción
    nueva_accion = Action(
        dni_usuario=evento.dni_usuario,
        provider_id=evento.provider_id,
        tipo_material=evento.tipo_material,
        cantidad_kg=evento.cantidad_kg,
        puntos_generados=puntos_ganados
    )
    
    # 5. Guardamos todo en la base de datos
    db.add(nueva_accion)
    db.commit()

    return {
        "estado": "Aceptado",
        "usuario": usuario.nombre,
        "puntos_ganados": puntos_ganados,
        "nuevo_saldo_total": usuario.saldo_puntos
    }