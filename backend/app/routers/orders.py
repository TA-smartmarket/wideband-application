"""Order endpoints (create + list)."""
from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import select
from sqlalchemy.orm import Session

from ..db import get_db
from ..deps import get_current_user
from ..models import Order, OrderItem, Product, User
from ..schemas import OrderCreateRequest, OrderItemOut, OrderOut

router = APIRouter(prefix="/api/v1/orders", tags=["orders"])


def _to_out(order: Order) -> OrderOut:
    return OrderOut(
        id=order.id,
        user_id=order.user_id,
        items=[
            OrderItemOut(
                product_id=it.product_id,
                name=it.name,
                quantity=it.quantity,
                unit_price=it.unit_price,
                subtotal=it.subtotal,
            )
            for it in order.items
        ],
        total=order.total,
        created_at=order.created_at,
    )


@router.post("", response_model=OrderOut, status_code=status.HTTP_201_CREATED)
def create_order(
    payload: OrderCreateRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    # Validate every product exists and has sufficient stock up front.
    for item in payload.items:
        product = db.get(Product, item.product_id)
        if product is None:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Product {item.product_id} not found",
            )
        if item.quantity > product.stock:
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail=f"Insufficient stock for {product.name}",
            )

    order = Order(user_id=current_user.id, total=0)
    db.add(order)
    db.flush()

    items_out: list[OrderItemOut] = []
    total = 0
    for item in payload.items:
        product = db.get(Product, item.product_id)
        subtotal = product.price * item.quantity
        product.stock -= item.quantity
        db.add(
            OrderItem(
                order_id=order.id,
                product_id=product.id,
                name=product.name,
                quantity=item.quantity,
                unit_price=product.price,
                subtotal=subtotal,
            )
        )
        items_out.append(
            OrderItemOut(
                product_id=product.id,
                name=product.name,
                quantity=item.quantity,
                unit_price=product.price,
                subtotal=subtotal,
            )
        )
        total += subtotal

    order.total = total
    db.commit()
    db.refresh(order)
    return OrderOut(
        id=order.id,
        user_id=order.user_id,
        items=items_out,
        total=total,
        created_at=order.created_at,
    )


@router.get("", response_model=list[OrderOut])
def list_orders(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    stmt = (
        select(Order)
        .where(Order.user_id == current_user.id)
        .order_by(Order.created_at.desc())
    )
    orders = db.execute(stmt).scalars().all()
    return [_to_out(o) for o in orders]
