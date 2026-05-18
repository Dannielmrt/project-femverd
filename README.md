# Proyecto FemVerd

![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blue.svg?logo=kotlin)
![Android](https://img.shields.io/badge/Android-Jetpack%20Compose-3DDC84.svg?logo=android)
![Python](https://img.shields.io/badge/Python-3.11-3776AB.svg?logo=python)
![FastAPI](https://img.shields.io/badge/FastAPI-0.100-009688.svg?logo=fastapi)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791.svg?logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg?logo=docker)

**FemVerd** es una plataforma tecnológica distribuida (App Móvil + Backend API) diseñada para centralizar, registrar y gamificar las acciones de reciclaje ciudadano. La aplicación transforma el compromiso ambiental en un proceso medible y gratificante, conectando a los ciudadanos con la infraestructura de ecoparques locales a través de incentivos directos y beneficios fiscales.

---

## ✨ Características Principales

* **Interfaz Nativa Declarativa:** Cliente Android desarrollado 100% en Kotlin con **Jetpack Compose** y Material Design 3, garantizando una UX fluida y resiliencia ante cambios de estado (rotación de pantalla).
* **Ingesta de Datos M2M:** API asíncrona capaz de recibir payloads de máquinas de reciclaje externas (hardware) y procesar los puntos en hilos secundarios (`BackgroundTasks`) sin latencia.
* **Seguridad y Privacidad (RGPD):** * Cifrado en reposo del DNI mediante encriptación simétrica **Fernet (AES-128)**.
  * Búsquedas O(1) en base de datos implementando el patrón **Blind Index (SHA-256)** con *Pepper*.
  * Autenticación stateless mediante **JWT firmado asimétricamente (RS256)**.
  * Hash de contraseñas con **Bcrypt**.
* **Sistema de Recompensas:** Catálogo dinámico para canjear "Eco-Puntos" por bonos locales, generando códigos UUID únicos en una billetera digital.
* **Certificados Tributarios en PDF:** Motor de renderizado nativo en el dispositivo móvil para exportar resúmenes anuales de reciclaje válidos para desgravaciones fiscales.
* **Geolocalización Espacial:** Integración nativa con Google Maps SDK para localizar puntos verdes y ecoparques en tiempo real.
* **Auditoría Aislada:** Microservicio *daemon* independiente que registra trazas de seguridad mediante **Sockets TCP puros** y criptografía híbrida (RSA + Fernet).

---

## 🛠️ Stack Tecnológico

### Frontend (Android)
* **Lenguaje:** Kotlin
* **UI Toolkit:** Jetpack Compose
* **Arquitectura:** MVI (Model-View-Intent) / UDF con `StateFlow`
* **Red:** Retrofit 2 + Gson
* **Navegación:** Jetpack Navigation Compose
* **Utilidades:** ZXing (Generación QR dinámica)

### Backend (API)
* **Lenguaje:** Python 3.11
* **Framework:** FastAPI + Uvicorn
* **Persistencia:** SQLAlchemy 2.0 (ORM) + PostgreSQL 15
* **Seguridad:** PyJWT (RS256), Cryptography (Hazmat), Passlib (Bcrypt)
* **Validación:** Pydantic v2

### Infraestructura y Despliegue
* **Contenedores:** Docker & Docker Compose
* **Cloud:** AWS EC2 (Ubuntu Server)

---

## 📁 Estructura del Proyecto

El repositorio está organizado en un esquema de *Monorepo* que contiene las tres piezas principales del ecosistema:

```text
/
├── femverd-app/          # Cliente nativo Android (Kotlin/Compose)
├── femverd-api/          # Backend principal FastAPI
│   ├── app/              # Lógica de negocio, rutas y modelos ORM
│   ├── keys/             # Directorio de claves asimétricas RSA
│   └── Dockerfile        # Receta de compilación de la API
├── femverd-logger/       # Microservicio TCP Socket para auditoría
└── docker-compose.yml    # Orquestador del clúster (API + PostgreSQL + Logger)
```

---

## 🚀 Instalación y Despliegue Local (Backend)

La infraestructura del servidor está completamente dockerizada. Para levantar el entorno en tu máquina local o servidor:

### 1. Clonar el repositorio
```bash
git clone https://github.com/Dannielmrt/project-femverd.git
cd project-femverd
```

### 2. Configurar variables de entorno
Crea un archivo `.env` en el directorio `femverd-api/` basándote en el archivo `.env.example` incluido en el repositorio. Asegúrate de rellenar las claves de seguridad:

```ini
# - DATABASE CONFIGURATION
# Local Docker: DB_HOST=db | AWS EC2: DB_HOST=ip-publica-aws
DB_USER=admin
DB_PASSWORD=femverd_pass
DB_HOST=13.48.42.5
DB_PORT=5432
DB_NAME=femverd_db

# - SECURITY & ENCRYPTION
# Fernet Key for DNI encryption (Generar una clave Base64 válida de 32 bytes)
FERNET_KEY=<Tu_Clave_Fernet_Generada_En_Base64>
HASH_PEPPER=<Tu_Pepper_Aleatorio>
# M2M Secret Key for Ecoparks
API_KEY_ECOPARQUE=<Clave_De_Integracion_Externa>

# - JWT SETTINGS
JWT_ALGORITHM=RS256
ACCESS_TOKEN_EXPIRE_MINUTES=60

# - INFRASTRUCTURE (LOGGER)
LOGGER_HOST=logger
LOGGER_PORT=50000

# - GENERAL APP SETTINGS
DEBUG=True
PROJECT_NAME="FemVerd API"
VERSION=1.0.0
```

### 3. Orquestar los contenedores
Ejecuta Docker Compose para construir las imágenes, levantar PostgreSQL, inicializar el microservicio de logs y arrancar la API. *Nota: La API generará automáticamente sus pares de claves RSA en el primer arranque y poblará la base de datos (seeding).*

```bash
docker-compose up -d --build
```

* **Documentación API interactiva (Swagger UI):** Disponible en `http://localhost:8000/docs`

---

## 📱 Ejecución del Cliente Android

1. Abre el directorio `femverd-app` utilizando **Android Studio**.
2. Sincroniza las dependencias de Gradle.
3. Asegúrate de que la IP del archivo de configuración `RetrofitClient.kt` apunta a la dirección de tu servidor backend (`http://10.0.2.2:8000` si usas el emulador local de Android, o la IP pública de AWS si está en producción).
4. Compila y ejecuta el proyecto en un emulador (Recomendado: API 33) o dispositivo físico.

---

## 👨‍💻 Autor

**Daniel Moret Robledo**
* Proyecto Intermodular - 2º DAM (Desarrollo de Aplicaciones Multiplataforma)
* IES La Sénia - Paiporta
