import socket
import threading
import datetime

# listening on all network interfaces (0.0.0.0) so Docker containers can communicate
HOST = "0.0.0.0"
PORT = 50000  
LOG_FILE = "audit.log"

# threading lock to prevent race conditions
# If two threads try to write to the file at the exact same millisecond, 
# the lock forces them to go one by one, preventing corrupted text.
file_lock = threading.Lock()

def handle_client(conn, addr):
    """
    Handles a single connection from a client (like FastAPI).
    Runs in an independent background thread.
    """
    print(f"[NEW CONNECTION] Connected to {addr}")
    try:
        while True:
            # Receive data
            data = conn.recv(4096)
            if not data:
                break # Connection closed by the client

            message = data.decode('utf-8')
            timestamp = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            log_entry = f"[{timestamp}] SOURCE: {addr[0]} - EVENT: {message}\n"

            # critical section writing safely to the shared file
            with file_lock:
                with open(LOG_FILE, "a", encoding="utf-8") as f:
                    f.write(log_entry)

            print(f"[LOG RECORDED] {message}")
            
            # Send acknowledgment back to the client
            conn.sendall(b"[ACK] Log received safely")
            
    except Exception as e:
        print(f"[ERROR] Connection lost with {addr}: {e}")
    finally:
        conn.close()
        print(f"[DISCONNECTED] {addr}")

def start_server():
    """
    Main server loop. Listens for incoming TCP connections and 
    spawns a new daemon thread for each client.
    """
    print("[STARTING] Audit Logger Server is starting...")
    
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.bind((HOST, PORT))
        s.listen()
        print(f"[LISTENING] Server listening on {HOST}:{PORT}")

        while True:
            # Wait for an incoming connection
            conn, addr = s.accept()
            
            # Create and start a new daemon thread for the client
            thread = threading.Thread(target=handle_client, args=(conn, addr), daemon=True)
            thread.start()
            
            # Print active connections 
            print(f"[ACTIVE CONNECTIONS] {threading.active_count() - 1}")

if __name__ == "__main__":
    start_server()