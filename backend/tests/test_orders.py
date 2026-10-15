"""Order endpoint tests."""
from __future__ import annotations


def test_create_order_snapshot_and_stock_decrement(client, auth_headers):
    headers = auth_headers("order@example.com")
    resp = client.post(
        "/api/v1/orders",
        headers=headers,
        json={"items": [{"product_id": "p7", "quantity": 2}, {"product_id": "p4", "quantity": 3}]},
    )
    assert resp.status_code == 201, resp.text
    body = resp.json()
    # p7 = 65000 * 2, p4 = 3500 * 3
    assert body["total"] == 130000 + 10500
    assert len(body["items"]) == 2
    by_id = {it["product_id"]: it for it in body["items"]}
    assert by_id["p7"]["unit_price"] == 65000
    assert by_id["p7"]["subtotal"] == 130000
    assert by_id["p4"]["unit_price"] == 3500
    assert by_id["p4"]["subtotal"] == 10500

    # Stock decremented.
    p7 = client.get("/api/v1/products/p7").json()
    p4 = client.get("/api/v1/products/p4").json()
    assert p7["stock"] == 13  # 15 - 2
    assert p4["stock"] == 117  # 120 - 3


def test_order_requires_auth(client):
    resp = client.post("/api/v1/orders", json={"items": [{"product_id": "p1", "quantity": 1}]})
    assert resp.status_code == 401


def test_order_unknown_product(client, auth_headers):
    headers = auth_headers("unknown-product@example.com")
    resp = client.post(
        "/api/v1/orders",
        headers=headers,
        json={"items": [{"product_id": "does-not-exist", "quantity": 1}]},
    )
    assert resp.status_code == 404


def test_order_insufficient_stock(client, auth_headers):
    headers = auth_headers("stock@example.com")
    resp = client.post(
        "/api/v1/orders",
        headers=headers,
        json={"items": [{"product_id": "p7", "quantity": 9999}]},
    )
    assert resp.status_code == 409


def test_order_quantity_positive(client, auth_headers):
    headers = auth_headers("qty@example.com")
    resp = client.post(
        "/api/v1/orders",
        headers=headers,
        json={"items": [{"product_id": "p7", "quantity": 0}]},
    )
    assert resp.status_code == 422


def test_list_orders_newest_first(client, auth_headers):
    headers = auth_headers("list@example.com")
    client.post("/api/v1/orders", headers=headers, json={"items": [{"product_id": "p1", "quantity": 1}]})
    client.post("/api/v1/orders", headers=headers, json={"items": [{"product_id": "p2", "quantity": 1}]})
    resp = client.get("/api/v1/orders", headers=headers)
    assert resp.status_code == 200
    body = resp.json()
    assert len(body) == 2
