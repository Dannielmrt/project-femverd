from pydantic import BaseModel

# Schema for the expected data from the App or external sources
class UserCreate(BaseModel):
    user_name: str
    dni: str  # Request the raw DNI, then it will be stored encrypted in the DB