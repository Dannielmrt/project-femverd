from fastapi import FastAPI
from .database import engine, Base
from .models import user
from .models import action
from .routes import users, ingestion 
from app.models.material_rule import MaterialRule    
from app.models.green_point import GreenPoint        
from app.models.external_system import ExternalSystem

# Create tables if they do not exist
Base.metadata.create_all(bind=engine)

app = FastAPI(title="FemVerd API")

# Connect user and ingestion routes to the main API
app.include_router(users.router)
app.include_router(ingestion.router)

@app.get("/")
def home():
    return {"message": "FemVerd API 100% Operational"}