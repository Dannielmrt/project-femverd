# app/models/external_system.py
from sqlalchemy import Column, Integer, String
from ..database import Base

class ExternalSystem(Base):
    __tablename__ = "external_systems"

    id = Column(Integer, primary_key=True, index=True)
    provider_id = Column(String, unique=True, index=True, nullable=False)  # "ECO_VALENCIA_SUR"
    api_key_hash = Column(String, nullable=False)                          # Bcrypt hash of the API key
    adapter_type = Column(String, nullable=False)                          # "ecopark_v1"