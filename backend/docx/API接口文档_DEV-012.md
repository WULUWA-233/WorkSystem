# CRM System API Documentation

## Base URL

```
http://localhost:8080
```

## Unified Response Format

All APIs return:

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1717000000000
}
```

| Code | Description |
|------|-------------|
| 200 | Success |
| 400 | Bad Request / Parameter Error |
| 401 | Unauthorized (no token or invalid) |
| 403 | Forbidden (no permission) |
| 404 | Not Found |
| 1001 | Business Error |
| 1002 | Parameter Validation Error |
| 1003 | Data Not Found |
| 1004 | Duplicate Key |
| 1005 | Operation Failed |

## Authentication

**Header:** `Authorization: Bearer {token}`

Token obtained via `POST /api/v1/auth/login`. Token expires in 7200 seconds.

## Default Test Accounts

| Username | Password | Roles | Permissions |
|----------|----------|-------|-------------|
| admin | 123456 | ADMIN | All APIs |
| sales01 | 123456 | SALES | Customer, Opportunity, Follow, Order, Analytics |
| sales02 | 123456 | SALES | Same as sales01 |

## Permission Matrix

| Path Prefix | Required Role |
|-------------|:---:|
| `/api/v1/common/health` | Public |
| `/api/v1/auth/login` | Public |
| `/api/v1/auth/me`, `/api/v1/auth/logout` | Any authenticated |
| `/api/v1/system/users/**` | ADMIN |
| `/api/v1/system/roles/**` | ADMIN |
| `/api/v1/system/operation-logs/**` | ADMIN |
| `/api/v1/customer/**` | ADMIN, SALES |
| `/api/v1/sales/opportunities/**` | ADMIN, SALES |
| `/api/v1/sales/orders/**` | ADMIN, SALES |
| `/api/v1/analytics/**` | ADMIN, SALES |

## API Reference

### Auth & Common

| Method | Path | Auth | Description |
|--------|------|:---:|-------------|
| GET | `/api/v1/common/health` | No | Health check |
| POST | `/api/v1/auth/login` | No | User login |
| GET | `/api/v1/auth/me` | Yes | Current user |
| POST | `/api/v1/auth/logout` | Yes | Logout |

### User Management (ADMIN)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/system/users` | Pagination |
| GET | `/api/v1/system/users/{id}` | Detail |
| POST | `/api/v1/system/users` | Create |
| PUT | `/api/v1/system/users/{id}` | Update |
| DELETE | `/api/v1/system/users/{id}` | Delete |
| PATCH | `/api/v1/system/users/{id}/status` | Enable/Disable |
| PATCH | `/api/v1/system/users/{id}/password/reset` | Reset password |
| PUT | `/api/v1/system/users/{id}/roles` | Assign roles |

### Role Management (ADMIN)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/system/roles` | Pagination |
| GET | `/api/v1/system/roles/options` | Dropdown |
| GET | `/api/v1/system/roles/{id}` | Detail |
| POST | `/api/v1/system/roles` | Create |
| PUT | `/api/v1/system/roles/{id}` | Update |
| DELETE | `/api/v1/system/roles/{id}` | Delete |

### Customer Management (ADMIN/SALES)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/customer/customers` | Pagination |
| GET | `/api/v1/customer/customers/{id}` | Detail |
| POST | `/api/v1/customer/customers` | Create |
| PUT | `/api/v1/customer/customers/{id}` | Update |
| DELETE | `/api/v1/customer/customers/{id}` | Delete |
| PATCH | `/api/v1/customer/customers/{id}/status` | Status (0/1/2) |
| GET | `/api/v1/customer/customers/check-name` | Name check |

### Opportunity Management (ADMIN/SALES)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/sales/opportunities` | Pagination |
| GET | `/api/v1/sales/opportunities/{id}` | Detail |
| POST | `/api/v1/sales/opportunities` | Create |
| PUT | `/api/v1/sales/opportunities/{id}` | Update |
| DELETE | `/api/v1/sales/opportunities/{id}` | Delete |
| PATCH | `/api/v1/sales/opportunities/{id}/stage` | Stage (1-4) |
| PATCH | `/api/v1/sales/opportunities/{id}/win` | Mark won |
| PATCH | `/api/v1/sales/opportunities/{id}/lose` | Mark lost |
| GET | `/api/v1/customer/customers/{id}/opportunities` | By customer |

### Follow Record (ADMIN/SALES)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/customer/follow-records` | Pagination |
| POST | `/api/v1/customer/follow-records` | Create |
| GET | `/api/v1/customer/follow-records/{id}` | Detail |
| DELETE | `/api/v1/customer/follow-records/{id}` | Delete |
| GET | `/api/v1/customer/customers/{id}/follow-records` | By customer |
| GET | `/api/v1/sales/opportunities/{id}/follow-records` | By opportunity |

### Order Management (ADMIN/SALES)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/sales/orders` | Pagination |
| GET | `/api/v1/sales/orders/{id}` | Detail |
| POST | `/api/v1/sales/orders` | Create |
| PUT | `/api/v1/sales/orders/{id}` | Update |
| DELETE | `/api/v1/sales/orders/{id}` | Delete |
| PATCH | `/api/v1/sales/orders/{id}/status` | Status |
| PATCH | `/api/v1/sales/orders/{id}/pay` | Pay |
| PATCH | `/api/v1/sales/orders/{id}/ship` | Ship |
| PATCH | `/api/v1/sales/orders/{id}/complete` | Complete |
| PATCH | `/api/v1/sales/orders/{id}/cancel` | Cancel |
| GET | `/api/v1/customer/customers/{id}/orders` | By customer |
| GET | `/api/v1/sales/opportunities/{id}/orders` | By opportunity |

### Operation Log (ADMIN)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/system/operation-logs` | Pagination |
| GET | `/api/v1/system/operation-logs/{id}` | Detail |
| DELETE | `/api/v1/system/operation-logs/{id}` | Delete |

### Analytics (ADMIN/SALES)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/analytics/overview` | Dashboard |
| GET | `/api/v1/analytics/customers/new` | Customer trend |
| GET | `/api/v1/analytics/opportunities/stage` | Stage distribution |
| GET | `/api/v1/analytics/orders/status` | Order status |
| GET | `/api/v1/analytics/sales/performance` | Sales performance |
| GET | `/api/v1/analytics/customers/industry` | Industry |
| GET | `/api/v1/analytics/customers/region` | Region |

## Key Request Examples

### Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

### Create Customer

```bash
curl -X POST http://localhost:8080/api/v1/customer/customers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{"customerName":"ABC Corp","industry":"IT","ownerId":2,"status":1}'
```

### Create Opportunity

```bash
curl -X POST http://localhost:8080/api/v1/sales/opportunities \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{"customerId":1,"opportunityName":"CRM Project","amount":50000,"stage":1,"ownerId":2}'
```

## Swagger / Knife4j

Access at: `http://localhost:8080/doc.html`

All APIs include `Authorization: Bearer {token}` global header configuration.
