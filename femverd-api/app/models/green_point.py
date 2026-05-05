# app/models/green_point.py
from sqlalchemy import Column, Integer, String, Float
from ..database import Base

class GreenPoint(Base):
    __tablename__ = "green_points"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, nullable=False)
    latitude = Column(Float, nullable=False) # provisional (localization)
    longitude = Column(Float, nullable=False) # provisional (localization)
    point_type = Column(String, nullable=False)  # "ecopark"/"vending_machine"