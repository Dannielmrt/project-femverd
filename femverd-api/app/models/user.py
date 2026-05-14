from sqlalchemy import Column, Integer, String, Float, DateTime
from datetime import datetime, timezone
from ..database import Base 

class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    dni_hash = Column(String, unique=True, index=True, nullable=False) 
    encrypted_dni = Column(String, index=True) # No unique=True (dont make sense)
    email = Column(String, unique=True, index=True)  
    user_name = Column(String)
    points_balance = Column(Float, default=0.0)
    hashed_password = Column(String)
    created_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc))