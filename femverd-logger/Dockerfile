# femverd-logger/Dockerfile
FROM python:3.11-slim

WORKDIR /app

RUN pip install cryptography

# Copy the server script
COPY server.py .

# Run the server
CMD ["python", "-u", "server.py"]