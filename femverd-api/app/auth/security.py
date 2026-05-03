from fastapi import Security, HTTPException, status
from fastapi.security.api_key import APIKeyHeader
import os
from dotenv import load_dotenv

load_dotenv()

# Read the authentication key from environment variables
API_KEY = os.getenv("API_KEY_ECOPARQUE")

# Look for the "X-API-Key" header in the request
api_key_header = APIKeyHeader(name="X-API-Key", auto_error=False)

def verify_external_role(api_key: str = Security(api_key_header)):
    """
    ACL (Access Control List): Verify if the request has permission 
    to ingest data into the system.
    """
    if api_key != API_KEY:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Access denied. Insufficient permissions or invalid API Key."
        )
    return api_key