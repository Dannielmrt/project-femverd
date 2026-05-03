from sqlalchemy import Column, Integer, String, Float, DateTime
from datetime import datetime
from ..database import Base

class Action(Base):
    __tablename__ = "registro_acciones"

    id = Column(Integer, primary_key=True, index=True)
    dni_usuario = Column(String, index=True)  # A quién le sumamos los puntos
    provider_id = Column(String)              # Qué Ecoparque lo envió
    tipo_material = Column(String)
    cantidad_kg = Column(Float)
    puntos_generados = Column(Float)
    fecha = Column(DateTime, default=datetime.utcnow)