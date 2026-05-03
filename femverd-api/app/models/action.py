from sqlalchemy import Column, Integer, String, Float, DateTime
from datetime import datetime
from ..database import Base

class Action(Base):
    __tablename__ = "actions_record"

    id = Column(Integer, primary_key=True, index=True)
    user_dni = Column(String, index=True)  
    provider_id = Column(String)              
    material_type = Column(String)
    amount_kg = Column(Float)
    generated_points = Column(Float)
    date = Column(DateTime, default=datetime.utcnow)