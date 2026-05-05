from sqlalchemy import Column, Integer, String, Float, ForeignKey
from sqlalchemy.orm import relationship
from ..database import Base

class Action(Base):
    __tablename__ = "actions"

    id = Column(Integer, primary_key=True, index=True)
    user_dni = Column(String, nullable=False)  # Encrypted DNI
    amount_kg = Column(Float, nullable=False)
    generated_points = Column(Float, nullable=False)

    # FOREIGN KEYS
    # Linking to specific Green Point
    green_point_id = Column(Integer, ForeignKey("green_points.id"))
    # Linking to specific Material Rule
    material_rule_id = Column(Integer, ForeignKey("material_rules.id"))

    green_point = relationship("GreenPoint")
    material_rule = relationship("MaterialRule")