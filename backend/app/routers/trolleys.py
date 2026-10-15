"""Trolley navigation status endpoints.

This is an APP-side status store only. It does NOT send motor commands or talk
to real hardware; it records a pending navigation request and reports status.
"""
from __future__ import annotations

import json
from datetime import datetime, timezone

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from .. import config
from ..db import get_db
from ..deps import get_current_user
from ..models import Trolley, User
from ..schemas import NavigationRequest, TrolleyStatusOut

router = APIRouter(prefix="/api/v1/trolleys", tags=["trolleys"])

VALID_DESTINATIONS = {
    "d_sayur", "d_minuman", "d_daging", "d_snack", "d_sembako", "d_susu", "d_kasir",
}
VALID_NODES = {"start", "n1", "n2", "n3", "n4", "n5", "n6", "n7", "n8", "n9", "exit"}


def _is_stale(last_update: datetime | None) -> bool:
    if last_update is None:
        return True
    if last_update.tzinfo is None:
        last_update = last_update.replace(tzinfo=timezone.utc)
    return (datetime.now(timezone.utc) - last_update).total_seconds() > config.STALE_AFTER_SECONDS


def _to_status(trolley: Trolley) -> TrolleyStatusOut:
    route: list[str] = json.loads(trolley.route) if trolley.route else []
    return TrolleyStatusOut(
        id=trolley.id,
        status=trolley.status,
        destination_id=trolley.destination_id,
        route=route,
        route_index=trolley.route_index,
        last_update=trolley.last_update,
        stale=_is_stale(trolley.last_update),
    )


@router.post("/{trolley_id}/navigation", response_model=TrolleyStatusOut)
def set_navigation(
    trolley_id: str,
    payload: NavigationRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    if payload.destination_id not in VALID_DESTINATIONS:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail="Unknown destination",
        )
    if not all(node in VALID_NODES for node in payload.route):
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail="Route contains unknown node",
        )

    trolley = db.get(Trolley, trolley_id)
    if trolley is None:
        trolley = Trolley(id=trolley_id)
        db.add(trolley)

    trolley.status = "navigating"
    trolley.destination_id = payload.destination_id
    trolley.route = json.dumps(payload.route)
    trolley.route_index = 0
    trolley.last_update = datetime.now(timezone.utc)
    db.commit()
    db.refresh(trolley)
    return _to_status(trolley)


@router.get("/{trolley_id}/status", response_model=TrolleyStatusOut)
def get_status(
    trolley_id: str,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    trolley = db.get(Trolley, trolley_id)
    if trolley is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Trolley not found")
    return _to_status(trolley)
