"""FastAPI application entry point."""
from __future__ import annotations

from contextlib import asynccontextmanager

from fastapi import FastAPI

from .db import Base, SessionLocal, engine
from .routers import auth, orders, products, trolleys
from .seed import seed_products


@asynccontextmanager
async def lifespan(app: FastAPI):
    Base.metadata.create_all(bind=engine)
    db = SessionLocal()
    try:
        seed_products(db)
    finally:
        db.close()
    yield


app = FastAPI(title="Smart Market API", version="1.0.0", lifespan=lifespan)

app.include_router(auth.router)
app.include_router(products.router)
app.include_router(orders.router)
app.include_router(trolleys.router)


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}
