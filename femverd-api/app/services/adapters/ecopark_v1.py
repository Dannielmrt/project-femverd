from pydantic import BaseModel, Field, ValidationError
from fastapi import HTTPException
from .base import BaseAdapter, NormalizedEvent

class EcoparkPayload(BaseModel):
    ecopark_id: str
    citizen_doc: str
    waste_type: str
    weight_kg: float = Field(gt=0, description="Weight must be strictly greater than 0")

class EcoparkAdapter(BaseAdapter):
    def normalize(self, raw_data: dict) -> NormalizedEvent:
        try:
            # If raw_data is malformed, Pydantic raises a ValidationError
            valid_data = EcoparkPayload(**raw_data)
        except ValidationError as e:
            # Catch the Pydantic error and convert it to an HTTP 422 Unprocessable Entity
            raise HTTPException(status_code=422, detail=e.errors())

        material_mapping = {
            "plastico": "plastic",
            "vidrio": "glass",
            "pilas": "batteries",
            "carton": "cardboard"
        }
        
        internal_material = material_mapping.get(valid_data.waste_type.lower(), valid_data.waste_type.lower())
        
        return NormalizedEvent(
            provider_id=valid_data.ecopark_id,
            user_dni=valid_data.citizen_doc,
            material_type=internal_material,
            quantity=valid_data.weight_kg
        )