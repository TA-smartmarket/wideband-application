"""Trolley navigation/status endpoint tests."""
from __future__ import annotations


def test_navigation_valid(client, auth_headers):
    headers = auth_headers("nav@example.com")
    resp = client.post(
        "/api/v1/trolleys/t1/navigation",
        headers=headers,
        json={"destination_id": "d_sayur", "route": ["start", "n2", "n1"]},
    )
    assert resp.status_code == 200
    body = resp.json()
    assert body["status"] == "navigating"
    assert body["destination_id"] == "d_sayur"
    assert body["route"] == ["start", "n2", "n1"]
    assert body["route_index"] == 0
    assert body["stale"] is False


def test_navigation_invalid_destination(client, auth_headers):
    headers = auth_headers("bad-dest@example.com")
    resp = client.post(
        "/api/v1/trolleys/t1/navigation",
        headers=headers,
        json={"destination_id": "d_mars", "route": ["start", "n2"]},
    )
    assert resp.status_code == 422


def test_navigation_unknown_node(client, auth_headers):
    headers = auth_headers("bad-node@example.com")
    resp = client.post(
        "/api/v1/trolleys/t1/navigation",
        headers=headers,
        json={"destination_id": "d_sayur", "route": ["start", "n99"]},
    )
    assert resp.status_code == 422


def test_status_not_found(client, auth_headers):
    headers = auth_headers("missing@example.com")
    assert client.get("/api/v1/trolleys/none/status", headers=headers).status_code == 404


def test_status_requires_auth(client):
    assert client.get("/api/v1/trolleys/t1/status").status_code == 401
