# femverd-logger/Dockerfile
FROM python:3.11-slim

WORKDIR /app

RUN pip install cryptography

COPY server.py .

CMD ["python", "-u", "server.py"]