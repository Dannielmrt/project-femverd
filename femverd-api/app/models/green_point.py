from sqlalchemy import Column, Integer, String, Float, ForeignKey
from sqlalchemy.orm import relationship
from ..database import Base

class GreenPoint(Base):
    __tablename__ = "green_points"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, nullable=False)
    latitude = Column(Float, nullable=False)
    longitude = Column(Float, nullable=False)
    point_type = Column(String, nullable=False)
    
    address = Column(String, nullable=True)
    schedule = Column(String, nullable=True)
    accepted_materials = Column(String, nullable=True)

    provider_id = Column(String, ForeignKey("external_systems.provider_id"))
    provider = relationship("ExternalSystem", back_populates="green_points")