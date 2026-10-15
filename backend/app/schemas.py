"""Pydantic v2 request/response schemas."""
from __future__ import annotations

from datetime import datetime

from pydantic import BaseModel, ConfigDict, EmailStr, Field


# --------------------------------------------------------------------------- #
# Auth
# --------------------------------------------------------------------------- #
class RegisterRequest(BaseModel):
    name: str = Field(min_length=1, max_length=255)
    email: EmailStr
    password: str = Field(min_length=8)


class LoginRequest(BaseModel):
    email: EmailStr
    password: str


class UserOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: str
    name: str
    email: str


class AuthResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user: UserOut


# --------------------------------------------------------------------------- #
# Products
# --------------------------------------------------------------------------- #
class ProductOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: str
    name: str
    category_id: str
    description: str
    price: int
    stock: int
    emoji: str
    image_url: str | None


# --------------------------------------------------------------------------- #
# Orders
# --------------------------------------------------------------------------- #
class OrderItemIn(BaseModel):
    product_id: str
    quantity: int = Field(gt=0)


class OrderCreateRequest(BaseModel):
    items: list[OrderItemIn] = Field(min_length=1)


class OrderItemOut(BaseModel):
    product_id: str
    name: str
    quantity: int
    unit_price: int
    subtotal: int


class OrderOut(BaseModel):
    id: str
    user_id: str
    items: list[OrderItemOut]
    total: int
    created_at: datetime


# --------------------------------------------------------------------------- #
# Trolleys
# --------------------------------------------------------------------------- #
class NavigationRequest(BaseModel):
    destination_id: str
    route: list[str] = Field(min_length=1)


class TrolleyStatusOut(BaseModel):
    id: str
    status: str
    destination_id: str | None
    route: list[str]
    route_index: int
    last_update: datetime | None
    stale: bool
