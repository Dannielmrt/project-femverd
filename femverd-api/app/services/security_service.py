import os
from cryptography.fernet import Fernet
from dotenv import load_dotenv

load_dotenv()  # Load variables from .env file

# Retrieve the key from system environment variables
SECRET_KEY = os.getenv("FERNET_KEY").encode() 
cipher_suite = Fernet(SECRET_KEY)

def encrypt_dni(dni: str) -> str:
    """Converts plain text DNI into an encrypted string"""
    return cipher_suite.encrypt(dni.encode('utf-8')).decode('utf-8')

def decrypt_dni(encrypted_dni: str) -> str:
    """Decrypts the ciphered text back to the original DNI"""
    return cipher_suite.decrypt(encrypted_dni.encode('utf-8')).decode('utf-8')