from sqlalchemy import Column, Integer, String, Float
from ..database import Base 

class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    encrypted_dni = Column(String, unique=True, index=True)
    user_name = Column(String)
    points_balance = Column(Float, default=0.0)