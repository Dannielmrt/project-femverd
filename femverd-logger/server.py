import socket
import threading
import datetime
from cryptography.fernet import Fernet
from cryptography.hazmat.primitives.asymmetric import rsa, padding
from cryptography.hazmat.primitives import serialization, hashes

HOST = "0.0.0.0"
PORT = 50000
LOG_FILE = "audit.log"
file_lock = threading.Lock()

def generate_keys():
    """
    Generate private and public RSA keys for the server
    """
    private = rsa.generate_private_key(public_exponent=65537, key_size=2048)
    public = private.public_key()
    return private, public

def serialize_pub(pub):
    """
    Keep public key as a PEM format to send it to the client
    """
    return pub.public_bytes(
        encoding=serialization.Encoding.PEM,
        format=serialization.PublicFormat.SubjectPublicKeyInfo
    )

def decrypt_rsa(private, data):
    """
    Decrypt client data with server's private key
    """
    return private.decrypt(
        data,
        padding.OAEP(
            mgf=padding.MGF1(hashes.SHA256()),
            algorithm=hashes.SHA256(),
            label=None
        )
    )

def handle_client(conn, addr, private_key):
    """
    Handle secure connection with the client
    """
    print(f"[NEW CONNECTION] Connected to {addr}")
    try:
        # Send server public key to client
        conn.sendall(serialize_pub(private_key.public_key()))
        
        # Receive secret symmetric key encrypted, and decrypt it
        encrypted_sym_key = conn.recv(4096)
        sym_key = decrypt_rsa(private_key, encrypted_sym_key)
        f = Fernet(sym_key)
        
        print(f"[SECURE CHANNEL] Established with {addr}")

        while True:
            # Receive encrypted log data
            enc_data = conn.recv(4096)
            if not enc_data:
                break
                
            # Decrypt the log message using Fernet
            message = f.decrypt(enc_data).decode('utf-8')
            timestamp = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            log_entry = f"[{timestamp}] SOURCE: {addr[0]} - SECURE EVENT: {message}\n"

            # Write safely to the shared file
            with file_lock:
                with open(LOG_FILE, "a", encoding="utf-8") as file:
                    file.write(log_entry)

            print(f"[LOG RECORDED] {message}")
            
            # Send encrypted acknowledgment back
            ack_msg = "[ACK] Secure log received"
            conn.sendall(f.encrypt(ack_msg.encode('utf-8')))
            
    except Exception as e:
        print(f"[ERROR] Connection lost with {addr}: {e}")
    finally:
        conn.close()
        print(f"[DISCONNECTED] {addr}")

def start_server():
    private_key, public_key = generate_keys()
    print("[STARTING] Secure Audit Logger Server is starting...")
    print("[KEYPAIR] RSA keypair generated successfully.")
    
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.bind((HOST, PORT))
        s.listen()
        print(f"[LISTENING] Server listening on {HOST}:{PORT}")

        while True:
            conn, addr = s.accept()
            # Pass the private_key to the thread so it can decrypt the Fernet key
            thread = threading.Thread(target=handle_client, args=(conn, addr, private_key), daemon=True)
            thread.start()

if __name__ == "__main__":
    start_server()