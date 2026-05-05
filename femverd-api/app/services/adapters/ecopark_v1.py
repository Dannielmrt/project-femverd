# app/services/adapters/ecopark_v1.py
from .base import BaseAdapter, NormalizedEvent

class EcoparkAdapter(BaseAdapter):
    def normalize(self, raw_data: dict) -> NormalizedEvent:
        
        # Dictionary to translate the Ecopark's Spanish terms to our English standard
        material_mapping = {
            "plastico": "plastic",
            "vidrio": "glass",
            "pilas": "batteries",
            "carton": "cardboard"
        }
        
        # Extract the external material and translate it (defaults to the original if not found)
        external_material = raw_data.get("waste_type", "").lower()
        internal_material = material_mapping.get(external_material, external_material)
        
        # Map the external names to our clean format
        return NormalizedEvent(
            provider_id=raw_data.get("ecopark_id", "UNKNOWN_PROVIDER"),
            user_dni=raw_data.get("citizen_doc", ""),
            material_type=internal_material,
            amount_kg=float(raw_data.get("weight_kg", 0.0))
        )