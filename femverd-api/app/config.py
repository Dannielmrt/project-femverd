from pydantic_settings import BaseSettings, SettingsConfigDict
from functools import lru_cache

class Settings(BaseSettings):
    DB_USER: str
    DB_PASSWORD: str
    DB_HOST: str
    DB_PORT: int
    DB_NAME: str

    FERNET_KEY: str
    API_KEY_ECOPARQUE: str

    JWT_ALGORITHM: str = "RS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60

    LOGGER_HOST: str
    LOGGER_PORT: int = 5000

    DEBUG: bool = True
    PROJECT_NAME: str
    VERSION: str

    @property
    def DATABASE_URL(self) -> str:
        return f"postgresql://{self.DB_USER}:{self.DB_PASSWORD}@{self.DB_HOST}:{self.DB_PORT}/{self.DB_NAME}"

    model_config = SettingsConfigDict(env_file=".env")

@lru_cache()
def get_settings():
    """
    Returns application environment settings using caching to bypass file system access loops.
    """
    return Settings()

settings = get_settings()