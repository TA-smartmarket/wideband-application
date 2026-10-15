# Smart Market Backend

FastAPI + SQLAlchemy 2.0 backend for the Smart Market kiosk. JWT auth, product
catalog, order creation with stock management, and an app-side trolley status
store.

## Stack

- FastAPI, SQLAlchemy 2.0 (declarative), pydantic v2
- **MySQL/MariaDB** via `PyMySQL` (production default)
- **SQLite** fallback for tests only
- Passwords hashed with stdlib `pbkdf2_hmac` (SHA-256, 200k iterations, per-user salt)
- JWT access tokens (HS256) via PyJWT

## Setup

```bash
cd backend
python -m venv .venv
# Windows:
.venv\Scripts\activate
# POSIX:
source .venv/bin/activate

pip install -r requirements.txt
cp .env.example .env   # adjust DATABASE_URL / SECRET_KEY
```

## Run

```bash
uvicorn app.main:app --reload --port 8000
```

Interactive docs at <http://127.0.0.1:8000/docs>.

## Database: MySQL vs SQLite

`DATABASE_URL` defaults to `mysql+pymysql://root:@127.0.0.1:3306/smartmarket`.
Create the database first:

```sql
CREATE DATABASE smartmarket CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Tables are created and products seeded automatically on startup (idempotent).

> **Limitation:** when MySQL/MariaDB is unavailable (e.g. XAMPP server stopped),
> tests fall back to SQLite via `tests/conftest.py`. SQLite is for tests only —
> it does not exercise MySQL-specific behavior such as `DateTime(timezone=True)`.

## API

Base prefix `/api/v1`.

| Method | Path | Auth | Notes |
| ------ | ---- | ---- | ----- |
| POST | `/auth/register` | no | 201 → `{access_token, token_type, user}` |
| POST | `/auth/login` | no | 200 → same shape; 401 on bad credentials |
| GET | `/auth/me` | bearer | 200 → `{id, name, email}` |
| POST | `/auth/logout` | bearer | 204; stateless JWT, client discards token |
| GET | `/products` | no | `q`, `category` query params |
| GET | `/products/{id}` | no | 404 if missing |
| POST | `/orders` | bearer | snapshots prices, decrements stock, 409 on insufficient stock |
| GET | `/orders` | bearer | newest first |
| POST | `/trolleys/{id}/navigation` | bearer | validates destination + route node set |
| GET | `/trolleys/{id}/status` | bearer | includes `stale` flag (60s window) |

### Trolley endpoints are app-side only

`POST /trolleys/{id}/navigation` and `GET /trolleys/{id}/status` record and
report a pending navigation on the server. They do **not** send motor commands
or communicate with any real hardware (UWB anchors, ESP32, motors). Real
firmware integration is intentionally out of scope and must be added behind a
separate hardware adapter.

## Tests

```bash
pytest -q
```

Tests use an isolated SQLite database per run (see `tests/conftest.py`).
