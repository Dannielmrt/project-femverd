def calculate_points(points_per_unit: float, amount: float) -> float:
    """
    Multiplies the amount (e.g., kg) by the multiplier defined in the database.
    """
    return amount * points_per_unit