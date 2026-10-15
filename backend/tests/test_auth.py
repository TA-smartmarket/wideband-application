"""Auth endpoint tests."""
from __future__ import annotations


def test_register_success(client):
    resp = client.post(
        "/api/v1/auth/register",
        json={"name": "Alice", "email": "alice@example.com", "password": "password123"},
    )
    assert resp.status_code == 201
    body = resp.json()
    assert body["token_type"] == "bearer"
    assert body["access_token"]
    assert body["user"]["id"]
    assert body["user"]["name"] == "Alice"
    assert body["user"]["email"] == "alice@example.com"


def test_register_duplicate_email(client):
    payload = {"name": "Bob", "email": "bob@example.com", "password": "password123"}
    assert client.post("/api/v1/auth/register", json=payload).status_code == 201
    resp = client.post("/api/v1/auth/register", json=payload)
    assert resp.status_code == 409


def test_register_short_password(client):
    resp = client.post(
        "/api/v1/auth/register",
        json={"name": "Carl", "email": "carl@example.com", "password": "short"},
    )
    assert resp.status_code == 422


def test_register_invalid_email(client):
    resp = client.post(
        "/api/v1/auth/register",
        json={"name": "Dan", "email": "not-an-email", "password": "password123"},
    )
    assert resp.status_code == 422


def test_login_failure_wrong_password(client):
    client.post(
        "/api/v1/auth/register",
        json={"name": "Eve", "email": "eve@example.com", "password": "password123"},
    )
    resp = client.post(
        "/api/v1/auth/login",
        json={"email": "eve@example.com", "password": "wrong-password"},
    )
    assert resp.status_code == 401


def test_login_success(client):
    client.post(
        "/api/v1/auth/register",
        json={"name": "Frank", "email": "frank@example.com", "password": "password123"},
    )
    resp = client.post(
        "/api/v1/auth/login",
        json={"email": "frank@example.com", "password": "password123"},
    )
    assert resp.status_code == 200
    body = resp.json()
    assert body["access_token"]
    assert body["user"]["email"] == "frank@example.com"


def test_me_without_token(client):
    assert client.get("/api/v1/auth/me").status_code == 401


def test_me_with_token(client, auth_headers):
    headers = auth_headers("me@example.com")
    resp = client.get("/api/v1/auth/me", headers=headers)
    assert resp.status_code == 200
    assert resp.json()["email"] == "me@example.com"


def test_logout_requires_token(client):
    assert client.post("/api/v1/auth/logout").status_code == 401


def test_logout_ok(client, auth_headers):
    headers = auth_headers("logout@example.com")
    assert client.post("/api/v1/auth/logout", headers=headers).status_code == 204
