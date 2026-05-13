from sqlalchemy import Column, Integer, String, Float, ForeignKey, DateTime
from datetime import datetime, timezone
from sqlalchemy.orm import relationship
from ..database import Base

class Action(Base):
    __tablename__ = "actions"

    id = Column(Integer, primary_key=True, index=True)
    user_dni = Column(String, nullable=False)  # Encrypted DNI
    quantity = Column(Float, nullable=False) 
    generated_points = Column(Float, nullable=False)
    created_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc))

    green_point_id = Column(Integer, ForeignKey("green_points.id"))
    material_rule_id = Column(Integer, ForeignKey("material_rules.id"))

    green_point = relationship("GreenPoint")
    material_rule = relationship("MaterialRule")