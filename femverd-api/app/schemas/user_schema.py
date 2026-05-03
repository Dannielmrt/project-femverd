from pydantic import BaseModel

# Este es el molde de lo que ESPERAMOS recibir de la App o externos
class UserCreate(BaseModel):
    nombre: str
    dni: str  # Pedimos el DNI normal, luego en la base de datos se guardará cifrado