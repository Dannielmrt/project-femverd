# app/services/adapters/base.py
from abc import ABC, abstractmethod
from pydantic import BaseModel

# ALL adapters must convert external JSONs into this format.
class NormalizedEvent(BaseModel):
    provider_id: str
    user_dni: str
    material_type: str
    quantity: float

class BaseAdapter(ABC):
    @abstractmethod
    def normalize(self, raw_data: dict) -> NormalizedEvent:
        """
        Takes a raw dictionary from an external system and 
        returns a clean NormalizedEvent.
        """
        pass