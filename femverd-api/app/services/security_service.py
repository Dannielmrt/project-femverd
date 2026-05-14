import hashlib
import os
from cryptography.fernet import Fernet
from dotenv import load_dotenv

load_dotenv()  # Load variables from .env file

PEPPER_STR = os.getenv("HASH_PEPPER")
if not PEPPER_STR:
    raise ValueError("FATAL ERROR: HASH_PEPPER is missing from .env file")

PEPPER = PEPPER_STR.encode()

# Retrieve the key from system environment variables
SECRET_KEY = os.getenv("FERNET_KEY").encode() 
cipher_suite = Fernet(SECRET_KEY)

def encrypt_dni(dni: str) -> str:
    """Converts plain text DNI into an encrypted string"""
    return cipher_suite.encrypt(dni.encode('utf-8')).decode('utf-8')

def decrypt_dni(encrypted_dni: str) -> str:
    """Decrypts the ciphered text back to the original DNI"""
    return cipher_suite.decrypt(encrypted_dni.encode('utf-8')).decode('utf-8')

def hash_dni(dni: str) -> str:
    """Generate a hash to searcg in the DB"""
    return hashlib.sha256(dni.encode('utf-8') + PEPPER).hexdigest()