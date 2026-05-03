# femverd-api/app/services/points_service.py

def calculate_points(material: str, kg: float) -> float:
    """
    Motor central de recompensas. 
    (En el futuro esto leerá de la tabla 'tipo_accion' de la DB)
    """
    reglas = {
        "plastico": 15.0, # 15 puntos por KG
        "vidrio": 5.0,
        "pilas": 50.0
    }
    
    # Si el material no existe, da 0 puntos
    multiplicador = reglas.get(material.lower(), 0.0)
    puntos = kg * multiplicador
    
    return puntos