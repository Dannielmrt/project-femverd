# app/services/adapters/ecopark_v1.py
from .base import BaseAdapter, NormalizedEvent

class EcoparkAdapter(BaseAdapter):
    def normalize(self, raw_data: dict) -> NormalizedEvent:
        # We map the external weird names to our clean, internal names
        return NormalizedEvent(
            provider_id=raw_data.get("ecopark_id", "UNKNOWN_PROVIDER"),
            user_dni=raw_data.get("citizen_doc", ""),
            material_type=raw_data.get("waste_type", ""),
            amount_kg=float(raw_data.get("weight_kg", 0.0))
        )