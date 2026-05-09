# app/services/auth_service.py
import jwt
import os
from datetime import datetime, timedelta
from typing import Optional, Dict, Any

# os.path to build reliable absolute paths to our keys folder
BASE_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
PRIVATE_KEY_PATH = os.path.join(BASE_DIR, "keys", "private_key.pem")
PUBLIC_KEY_PATH = os.path.join(BASE_DIR, "keys", "public_key.pem")

# Load the keys into memory when the API starts
with open(PRIVATE_KEY_PATH, "r") as f:
    PRIVATE_KEY = f.read()

with open(PUBLIC_KEY_PATH, "r") as f:
    PUBLIC_KEY = f.read()

ALGORITHM = "RS256" # Asymmetric RSA signature
ACCESS_TOKEN_EXPIRE_MINUTES = 60 # The app token will be valid for 1 hour

def create_access_token(data: dict) -> str:
    """
    Creates a JWT signed with the server's Private RSA Key.
    """
    to_encode = data.copy()
    
    # Calculate expiration time
    expire = datetime.utcnow() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode.update({"exp": expire})
    
    # Sign the token using RS256 and the private key
    encoded_jwt = jwt.encode(to_encode, PRIVATE_KEY, algorithm=ALGORITHM)
    return encoded_jwt

def verify_token(token: str) -> Optional[Dict[str, Any]]:
    """
    Verifies a JWT using the server's Public RSA Key.
    Returns the decoded payload if valid, or None if invalid/expired.
    """
    try:
        # try to decode it and if the signature was tampered with, this will fail.
        payload = jwt.decode(token, PUBLIC_KEY, algorithms=[ALGORITHM])
        return payload
    except jwt.ExpiredSignatureError:
        print("[AUTH ERROR] Token has expired")
        return None
    except jwt.InvalidTokenError:
        print("[AUTH ERROR] Invalid token signature")
        return None