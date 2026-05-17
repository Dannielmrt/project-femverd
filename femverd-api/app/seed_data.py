# app/seed_data.py
import bcrypt
from app.database import SessionLocal
from app.models.material_rule import MaterialRule
from app.models.green_point import GreenPoint
from app.models.external_system import ExternalSystem

def seed_database():
    """
    Populates the database with initial test data.
    """
    db = SessionLocal()
    
    try:
        # Create the External System (Provider) with a hashed API Key
        if not db.query(ExternalSystem).first():
            print("Seeding External Systems...")
            raw_password = b"clave-secreta-m2m-123"
            salt = bcrypt.gensalt()
            hashed_key = bcrypt.hashpw(raw_password, salt).decode('utf-8')
            
            provider = ExternalSystem(
                provider_id="ECO_VALENCIA_SUR",
                api_key_hash=hashed_key,
                adapter_type="ecopark_v1"
            )
            db.add(provider)
            db.commit()

        # Create Green Points associated with the Provider for the Map
        if not db.query(GreenPoint).first():
            print("Seeding Green Points (Map Locations)...")
            
            ecoparks_data = [
                {
                    "name": "Central Station Ecopark",
                    "latitude": 39.4699,
                    "longitude": -0.3763,
                    "address": "Plaza del Ayuntamiento, 1",
                    "schedule": "L-V: 09:00 - 20:00 | S-D: 10:00 - 14:00"
                },
                {
                    "name": "North University Campus Hub",
                    "latitude": 39.4815,
                    "longitude": -0.3472,
                    "address": "Av. dels Tarongers, s/n",
                    "schedule": "L-V: 08:00 - 18:00 | S: 09:00 - 13:00"
                },
                {
                    "name": "South Port Recycling Point",
                    "latitude": 39.4520,
                    "longitude": -0.3315,
                    "address": "Marina Real, Muelle 4",
                    "schedule": "24/7"
                },
                {
                    "name": "West Tech Park GreenPoint",
                    "latitude": 39.4890,
                    "longitude": -0.4120,
                    "address": "Ronda Norte, 45",
                    "schedule": "L-D: 08:00 - 22:00"
                }
            ]

            for park in ecoparks_data:
                ecopark = GreenPoint(
                    name=park["name"],
                    latitude=park["latitude"],
                    longitude=park["longitude"],
                    point_type="ecopark",
                    address=park["address"], 
                    schedule=park["schedule"], 
                    accepted_materials="Plástico, Vidrio, Pilas, Aceite, Muebles", 
                    provider_id="ECO_VALENCIA_SUR" 
                )
                db.add(ecopark)
            db.commit()

        # Create the Material Rules
        if not db.query(MaterialRule).first():
            print("Seeding Material Rules...")
            rules = [
                MaterialRule(material_name="plastic", points_per_unit=15.0, unit_type="kg"),
                MaterialRule(material_name="glass", points_per_unit=5.0, unit_type="kg"),
                MaterialRule(material_name="batteries", points_per_unit=50.0, unit_type="kg")
            ]
            db.add_all(rules)
            db.commit()

        print("Database seeding completed successfully!")

    except Exception as e:
        print(f"An error occurred during seeding: {e}")
        db.rollback()
    finally:
        db.close()

if __name__ == "__main__":
    seed_database()