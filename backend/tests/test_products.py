"""Product catalog endpoint tests."""
from __future__ import annotations


def test_list_products(client):
    resp = client.get("/api/v1/products")
    assert resp.status_code == 200
    body = resp.json()
    assert len(body) == 18
    fields = {"id", "name", "category_id", "description", "price", "stock", "emoji", "image_url"}
    for item in body:
        assert fields <= set(item.keys())
        assert isinstance(item["price"], int)
        assert isinstance(item["stock"], int)


def test_product_filter_by_category(client):
    resp = client.get("/api/v1/products", params={"category": "sayur"})
    assert resp.status_code == 200
    body = resp.json()
    assert len(body) == 3
    assert all(p["category_id"] == "sayur" for p in body)


def test_product_search(client):
    resp = client.get("/api/v1/products", params={"q": "beras"})
    assert resp.status_code == 200
    body = resp.json()
    assert len(body) == 1
    assert body[0]["id"] == "p13"


def test_product_detail(client):
    resp = client.get("/api/v1/products/p7")
    assert resp.status_code == 200
    body = resp.json()
    assert body["id"] == "p7"
    assert body["name"] == "Daging Sapi 500g"
    assert body["price"] == 65000


def test_product_not_found(client):
    assert client.get("/api/v1/products/nope").status_code == 404
