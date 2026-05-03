from sqlalchemy import Column, Integer, String, Float
from ..database import Base  # El ".." es para subir un nivel de carpeta

class User(Base):
    __tablename__ = "usuarios"

    id = Column(Integer, primary_key=True, index=True)
    dni_cifrado = Column(String, unique=True, index=True)
    nombre = Column(String)
    saldo_puntos = Column(Float, default=0.0)