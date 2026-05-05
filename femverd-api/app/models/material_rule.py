# app/models/material_rule.py
from sqlalchemy import Column, Integer, String, Float
from ..database import Base

class MaterialRule(Base):
    __tablename__ = "material_rules"

    id = Column(Integer, primary_key=True, index=True)
    material_name = Column(String, unique=True, index=True, nullable=False)  # "plastic"
    points_per_unit = Column(Float, nullable=False)                          # 15.0
    unit_type = Column(String, nullable=False)                               # "kg" or "ud"