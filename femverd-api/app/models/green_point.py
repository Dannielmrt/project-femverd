# app/models/green_point.py
from sqlalchemy import Column, Integer, String, Float, ForeignKey
from sqlalchemy.orm import relationship
from ..database import Base

class GreenPoint(Base):
    __tablename__ = "green_points"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, nullable=False)
    latitude = Column(Float, nullable=False) # provisional (localization)
    longitude = Column(Float, nullable=False) # provisional (localization)
    point_type = Column(String, nullable=False)  # "ecopark"/"vending_machine"

    # FOREIGN KEY Linking to the external system's unique provider_id
    provider_id = Column(String, ForeignKey("external_systems.provider_id"))
    # N Green Points -> 1 Provider
    provider = relationship("ExternalSystem", back_populates="green_points")