"""Category definitions and idempotent product seeding."""
from __future__ import annotations

from sqlalchemy import select
from sqlalchemy.orm import Session

from .models import Product

CATEGORIES: dict[str, str] = {
    "sayur": "Sayur & Buah",
    "minuman": "Minuman",
    "daging": "Daging & Seafood",
    "snack": "Snack & Roti",
    "sembako": "Sembako",
    "susu": "Susu & Telur",
}

# (id, name, category_id, price, stock, emoji, description)
PRODUCTS: list[tuple[str, str, str, int, int, str, str]] = [
    ("p1", "Wortel Segar 500g", "sayur", 8500, 40, "🥕", "Wortel segar kemasan 500 gram."),
    ("p2", "Brokoli 300g", "sayur", 12000, 25, "🥦", "Brokoli hijau segar 300 gram."),
    ("p3", "Apel Fuji 1kg", "sayur", 35000, 18, "🍎", "Apel Fuji pilihan 1 kilogram."),
    ("p4", "Air Mineral 600ml", "minuman", 3500, 120, "💧", "Air mineral kemasan 600 ml."),
    ("p5", "Teh Botol 350ml", "minuman", 5000, 80, "🍵", "Teh botol siap minum 350 ml."),
    ("p6", "Jus Jeruk 1L", "minuman", 18000, 30, "🍊", "Jus jeruk segar 1 liter."),
    ("p7", "Daging Sapi 500g", "daging", 65000, 15, "🥩", "Daging sapi segar 500 gram."),
    ("p8", "Ayam Utuh 1kg", "daging", 38000, 20, "🍗", "Ayam utuh segar 1 kilogram."),
    ("p9", "Udang Segar 250g", "daging", 42000, 12, "🦐", "Udang segar 250 gram."),
    ("p10", "Keripik Kentang 68g", "snack", 11000, 60, "🍟", "Keripik kentang 68 gram."),
    ("p11", "Roti Tawar 400g", "snack", 15000, 35, "🍞", "Roti tawar 400 gram."),
    ("p12", "Cokelat Batang 90g", "snack", 16000, 40, "🍫", "Cokelat batang 90 gram."),
    ("p13", "Beras Premium 5kg", "sembako", 68000, 22, "🍚", "Beras premium 5 kilogram."),
    ("p14", "Minyak Goreng 2L", "sembako", 32000, 28, "🫒", "Minyak goreng 2 liter."),
    ("p15", "Gula Pasir 1kg", "sembako", 17000, 45, "🍬", "Gula pasir 1 kilogram."),
    ("p16", "Susu Kotak 1L", "susu", 21000, 50, "🥛", "Susu UHT kotak 1 liter."),
    ("p17", "Telur Ayam 1kg", "susu", 28000, 26, "🥚", "Telur ayam 1 kilogram."),
    ("p18", "Keju Cheddar 170g", "susu", 24000, 19, "🧀", "Keju cheddar 170 gram."),
]


def seed_products(db: Session) -> None:
    """Insert the fixed product set if the table is empty (idempotent)."""
    existing = db.execute(select(Product.id)).scalars().first()
    if existing is not None:
        return
    for pid, name, category_id, price, stock, emoji, description in PRODUCTS:
        db.add(
            Product(
                id=pid,
                name=name,
                category_id=category_id,
                description=description,
                price=price,
                stock=stock,
                emoji=emoji,
                image_url=None,
            )
        )
    db.commit()
