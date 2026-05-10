# app/main.py
import os
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography.hazmat.primitives import serialization
from fastapi import FastAPI

# Generate keys (JWT, for the app)
APP_DIR = os.path.dirname(os.path.abspath(__file__)) #  'app/'
PROJECT_ROOT = os.path.dirname(APP_DIR)              #  'femverd-api/'
KEYS_DIR = os.path.join(PROJECT_ROOT, "keys")

def ensure_rsa_keys_exist():
    private_key_path = os.path.join(KEYS_DIR, "private_key.pem")
    public_key_path = os.path.join(KEYS_DIR, "public_key.pem")

    os.makedirs(KEYS_DIR, exist_ok=True)

    if not os.path.exists(private_key_path) or not os.path.exists(public_key_path):
        print(f"RSA Keys not found. Generating new keys in: {KEYS_DIR}")
        private_key = rsa.generate_private_key(public_exponent=65537, key_size=2048)
        
        with open(private_key_path, "wb") as f:
            f.write(private_key.private_bytes(
                encoding=serialization.Encoding.PEM,
                format=serialization.PrivateFormat.PKCS8,
                encryption_algorithm=serialization.NoEncryption()
            ))
            
        public_key = private_key.public_key()
        with open(public_key_path, "wb") as f:
            f.write(public_key.public_bytes(
                encoding=serialization.Encoding.PEM,
                format=serialization.PublicFormat.SubjectPublicKeyInfo
            ))
        print("Keys generated successfully!")
    else:
        print("Existing RSA keys found. Startup continuing.")

ensure_rsa_keys_exist()

from .database import engine, Base
from .models import user, action
from .routes import users, ingestion, auth
from app.models.material_rule import MaterialRule    
from app.models.green_point import GreenPoint        
from app.models.external_system import ExternalSystem

# Create tables if they do not exist
Base.metadata.create_all(bind=engine)

app = FastAPI(title="FemVerd API")

# Connect user and ingestion routes to the main API
app.include_router(users.router)
app.include_router(ingestion.router)
app.include_router(auth.router)

@app.get("/")
def home():
    return {"message": "FemVerd API 100% Operational"}