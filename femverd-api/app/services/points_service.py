# femverd-api/app/services/points_service.py

def calculate_points(material: str, kg: float) -> float:
    """
    Core rewards engine. 
    (In the future, this will fetch data from the 'action_types' DB table)
    """
    reward_rates = {
        "plastic": 15.0, # 15 points per KG
        "glass": 5.0,
        "batteries": 50.0
    }
    
    # If the material is not recognized, return 0.0 points
    multiplier = reward_rates.get(material.lower(), 0.0)
    points = kg * multiplier
    
    return points