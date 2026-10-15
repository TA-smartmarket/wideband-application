"""Pytest fixtures: SQLite test DB + seeded products, isolated per test."""
from __future__ import annotations

import os
import pathlib

# Must be set BEFORE importing app modules so db.py/config.py pick them up.
TEST_DB = pathlib.Path(__file__).parent / "smartmarket_test.db"
os.environ["DATABASE_URL"] = f"sqlite+pysqlite:///{TEST_DB.as_posix()}"
os.environ["SECRET_KEY"] = "test-secret-key-not-for-production"

import pytest
from fastapi.testclient import TestClient

from app.db import Base, SessionLocal, engine
from app.main import app
from app.seed import seed_products


def reset_db() -> None:
    Base.metadata.drop_all(bind=engine)
    Base.metadata.create_all(bind=engine)
    db = SessionLocal()
    try:
        seed_products(db)
    finally:
        db.close()


@pytest.fixture(autouse=True)
def _clean_db():
    reset_db()
    yield
    reset_db()


@pytest.fixture()
def client():
    return TestClient(app)


@pytest.fixture()
def auth_headers(client):
    """Register a throwaway user and return its Authorization header dict."""

    def _login(email: str = "tester@example.com", password: str = "password123") -> dict:
        resp = client.post(
            "/api/v1/auth/register",
            json={"name": "Tester", "email": email, "password": password},
        )
        assert resp.status_code == 201, resp.text
        token = resp.json()["access_token"]
        return {"Authorization": f"Bearer {token}"}

    return _login
