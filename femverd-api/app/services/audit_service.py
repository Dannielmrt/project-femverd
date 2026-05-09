# app/services/audit_service.py
import socket
import os
from cryptography.fernet import Fernet
from cryptography.hazmat.primitives import serialization, hashes
from cryptography.hazmat.primitives.asymmetric import padding

# local testing from inside Docker to host terminal -> "host.docker.internal" [solo para pruebas]
LOGGER_HOST = os.getenv("LOGGER_HOST", "host.docker.internal")
LOGGER_PORT = int(os.getenv("LOGGER_PORT", 50000))

def load_pub(pem):
    return serialization.load_pem_public_key(pem)

def encrypt_rsa(pub, data):
    return pub.encrypt(
        data,
        padding.OAEP(
            mgf=padding.MGF1(hashes.SHA256()),
            algorithm=hashes.SHA256(),
            label=None
        )
    )

def send_audit_log(message: str):
    """
    Connects to the secure TCP Logger and sends an encrypted audit event.
    """
    try:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.settimeout(3.0) 
            s.connect((LOGGER_HOST, LOGGER_PORT))
            
            # Receive server's public RSA key
            pub_pem = s.recv(4096)
            pub = load_pub(pub_pem)
            
            # Generate symmetric secret key (Fernet)
            sym = Fernet.generate_key()
            f = Fernet(sym)
            
            # Encrypt symmetric key with RSA and send to server
            s.sendall(encrypt_rsa(pub, sym))
            
            # Send the actual log message encrypted with Fernet
            s.sendall(f.encrypt(message.encode('utf-8')))
            
            # Wait for encrypted acknowledgment
            s.recv(4096)
            
    except Exception as e:
        # catch the error but don't raise it
        print(f"[AUDIT SERVICE WARNING] Could not send log: {e}", flush=True)