FROM python:3.11-slim

# Work directory inside the container
WORKDIR /app

# Copy the archive that contains the libraries
COPY requirements.txt .

# Install the libraries
RUN pip install --no-cache-dir -r requirements.txt

# Copiamos el contenido de nuestra carpeta app local a la carpeta /app/app del contenedor
# Copy the content of the local app directory to the directory of the container
COPY ./app ./app

# Start Uvicorn
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000", "--reload"]