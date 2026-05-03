from fastapi import FastAPI
from .database import engine, Base
from .models import user
from .models import action
from .routes import users, ingestion  # <--- Importamos las rutas que acabamos de crear

# Crea las tablas si no existen
Base.metadata.create_all(bind=engine)

app = FastAPI(title="FemVerd API")

# <--- Conectamos las rutas de usuarios a nuestra API principal
app.include_router(users.router)
app.include_router(ingestion.router)

@app.get("/")
def home():
    return {"message": "API FemVerd 100% Operativa"}