"""Environment-driven application configuration."""
from __future__ import annotations

import os

from dotenv import load_dotenv

load_dotenv()

# MySQL-first via SQLAlchemy; SQLite is used only as a test convenience.
DATABASE_URL: str = os.getenv(
    "DATABASE_URL",
    "mysql+pymysql://root:@127.0.0.1:3306/smartmarket",
)
SECRET_KEY: str = os.getenv("SECRET_KEY", "change-me-to-a-long-random-string")
ALGORITHM: str = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES: int = int(os.getenv("ACCESS_TOKEN_EXPIRE_MINUTES", "60"))

# Trolley status is considered stale after this many seconds without an update.
STALE_AFTER_SECONDS: int = int(os.getenv("STALE_AFTER_SECONDS", "60"))
