from sqlalchemy import Column, Integer, String, Float, DateTime, ForeignKey
from datetime import datetime, timezone
from app.database import Base

class Redemption(Base):
    __tablename__ = "redemptions"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    reward_name = Column(String, nullable=False)
    points_cost = Column(Float, nullable=False)
    claim_code = Column(String, unique=True, nullable=False) # Reward code
    created_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc))