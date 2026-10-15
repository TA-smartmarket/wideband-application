"""Database engine and session management.

MySQL-first: the default ``DATABASE_URL`` points at MySQL/MariaDB. For tests the
environment may set ``DATABASE_URL`` to a SQLite URL (see ``tests/conftest.py``);
SQLite requires ``connect_args`` to allow cross-thread access.
"""
from __future__ import annotations

from sqlalchemy import create_engine
from sqlalchemy.orm import DeclarativeBase, sessionmaker

from . import config


class Base(DeclarativeBase):
    pass


_connect_args: dict = {}
if config.DATABASE_URL.startswith("sqlite"):
    _connect_args = {"check_same_thread": False}

engine = create_engine(
    config.DATABASE_URL,
    connect_args=_connect_args,
    pool_pre_ping=True,
)

SessionLocal = sessionmaker(bind=engine, autoflush=False, autocommit=False)


def get_db():
    """FastAPI dependency that yields a scoped database session."""
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
