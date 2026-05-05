# app/seed_data.py
import bcrypt
from app.database import SessionLocal
from app.models.material_rule import MaterialRule
from app.models.green_point import GreenPoint
from app.models.external_system import ExternalSystem

def seed_database():
    """
    Populates the database with initial test data.
    Run this script only once.
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

        # Create a Green Point associated with the Provider
        if not db.query(GreenPoint).first():
            print("Seeding Green Points...")
            ecopark = GreenPoint(
                name="Ecoparque Principal Valencia",
                latitude=39.4699,
                longitude=-0.3763,
                point_type="ecopark",
                provider_id="ECO_VALENCIA_SUR" # Foreign Key matching the provider
            )
            db.add(ecopark)
            db.commit()

        # Create the Material Rules (Business Logic)
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