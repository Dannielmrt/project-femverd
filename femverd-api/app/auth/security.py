# app/auth/security.py
from fastapi import Security, HTTPException, status, Depends
from fastapi.security.api_key import APIKeyHeader
from fastapi.security import OAuth2PasswordBearer
from app.services.auth_service import verify_token

api_key_header = APIKeyHeader(name="X-API-Key", auto_error=False)

# (JWT) tells FastAPI where the login route is
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/auth/login")

def get_api_key(api_key_header: str = Security(api_key_header)) -> str:
    """
    Extracts the API Key from the headers.
    The actual bcrypt validation happens in the route.
    """
    if not api_key_header:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Access denied. API Key missing."
        )
    return api_key_header

def get_current_user_token(token: str = Depends(oauth2_scheme)) -> str:
    """
    Extracts the JWT from the Authorization header, verifies the RSA signature,
    and returns the user's DNI if valid.
    """
    payload = verify_token(token)
    if not payload:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or expired token",
            headers={"WWW-Authenticate": "Bearer"},
        )
    
    user_dni = payload.get("sub")
    if not user_dni:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Token missing subject")
        
    return user_dni