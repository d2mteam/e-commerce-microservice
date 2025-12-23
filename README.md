# e-commerce-microservice (Order & Inventory)

Hướng dẫn nhanh để chạy dịch vụ và gọi API (không cần UI).

## Chạy dịch vụ
1) Khởi động Postgres và Kafka (xem docker-compose tại từng service). Port mặc định:
   - order-service DB: localhost:1000/order-service
   - inventory-service DB: localhost:2001/inventory-service
   - Kafka: localhost:9092
2) Tạo schema Akka persistence (journal/snapshot) nếu chưa có:
   ```bash
   psql -h localhost -p 1000 -U admin -d order-service -f akka-persistence-schema.sql
   psql -h localhost -p 2001 -U admin -d inventory-service -f akka-persistence-schema.sql
   ```
3) Chạy service:
   ```bash
   cd order-service && ./gradlew bootRun
   cd inventory-service && ./gradlew bootRun
   ```

## API Order (cổng mặc định 8081)
- Tạo order: `POST /orders`
  ```bash
  curl -X POST http://localhost:8081/orders \
    -H "Content-Type: application/json" \
    -d '{ "userId": "00000000-0000-0000-0000-000000000001",
          "orderDetails": [ { "productId": "00000000-0000-0000-0000-000000000111", "quantity": 1, "price": 100.0 } ] }'
  ```
- Hủy order: `DELETE /orders`
  ```bash
  curl -X DELETE http://localhost:8081/orders \
    -H "Content-Type: application/json" \
    -d '{ "aggregateId": "<orderId>", "userId": "00000000-0000-0000-0000-000000000001", "reason": "cancel" }'
  ```
- Lấy trạng thái: `GET /orders/{orderId}`
- Lấy event log: `GET /orders/{orderId}/events?limit=50`

### Chi tiết request/response (Order)
- `POST /orders`
  - Body: `{ "userId": "<uuid>", "orderDetails": [ { "productId": "<uuid>", "quantity": <int>, "price": <double> } ] }`
  - Trả về: JSON `OrderState`  
    ```
    {
      "orderId": "uuid",
      "userId": "uuid",
      "orderDetails": [ { "productId": "uuid", "quantity": int, "price": number } ],
      "status": "CREATED|CONFIRMED|OUT_OF_STOCK|CANCELLED|PAID",
      "inventoryId": "uuid|null",
      "createdAt": "ISO_INSTANT",
      "paidAt": "ISO_INSTANT|null"
    }
    ```
- `DELETE /orders`
  - Body: `{ "aggregateId": "<orderId>", "userId": "<uuid>", "reason": "<string>" }`
  - Trả về: `OrderState` (như trên, status thường = CANCELLED).
- `GET /orders/{orderId}`
  - Trả về: `OrderState`.
- `GET /orders/{orderId}/events?limit=50`
  - Trả về: mảng event  
    ```
    [
      {
        "persistenceId": "Order|<uuid>",
        "eventType": "<class name>",
        "sequenceNumber": long,
        "timestamp": "ISO_INSTANT",
        "event": { ...event payload... }
      },
      ...
    ]
    ```

## API Inventory (cổng mặc định 2000)
- Tạo inventory: `POST /api/inventory/create`
  ```bash
  curl -X POST http://localhost:2000/api/inventory/create \
    -H "Content-Type: application/json" \
    -d '{ "inventoryId": "00000000-0000-0000-0000-000000000111",
          "sku": "SKU-1", "initialQuantity": 10 }'
  ```
- Add stock: `POST /api/inventory/add-stock`
  ```bash
  curl -X POST http://localhost:2000/api/inventory/add-stock \
    -H "Content-Type: application/json" \
    -d '{ "inventoryId": "<inventoryId>", "quantity": 5 }'
  ```
- Reserve: `POST /api/inventory/reserve`
  ```bash
  curl -X POST http://localhost:2000/api/inventory/reserve \
    -H "Content-Type: application/json" \
    -d '{ "inventoryId": "<inventoryId>", "orderId": "<orderId>", "quantity": 1, "correlationId": "<optional>" }'
  ```
- Release: `POST /api/inventory/release`
  ```bash
  curl -X POST http://localhost:2000/api/inventory/release \
    -H "Content-Type: application/json" \
    -d '{ "inventoryId": "<inventoryId>", "orderId": "<orderId>" }'
  ```
- Lấy trạng thái: `GET /api/inventory/{inventoryId}`
- Lấy event log: `GET /api/inventory/{inventoryId}/events?limit=50`

### Chi tiết request/response (Inventory)
- `POST /api/inventory/create`
  - Body: `{ "inventoryId": "<uuid optional>", "sku": "<string>", "initialQuantity": <int> }`
  - Trả về: JSON `InventoryState`
    ```
    {
      "inventoryId": "uuid",
      "sku": "string",
      "availableQuantity": int,
      "reservedQuantity": int,
      "reservations": { "<orderId>": int } | null,
      "active": true|false,
      "createdAt": "ISO_INSTANT",
      "updatedAt": "ISO_INSTANT"
    }
    ```
- `POST /api/inventory/add-stock`
  - Body: `{ "inventoryId": "<uuid>", "quantity": <int> }`
  - Trả về: `InventoryState` sau khi add stock.
- `POST /api/inventory/reserve`
  - Body: `{ "inventoryId": "<uuid>", "orderId": "<uuid>", "quantity": <int>, "correlationId": "<optional>" }`
  - Trả về: `InventoryState` sau khi reserve.
- `POST /api/inventory/release`
  - Body: `{ "inventoryId": "<uuid>", "orderId": "<uuid>" }`
  - Trả về: `InventoryState` sau khi release.
- `POST /api/inventory/cancel-reservation`
  - Body: `{ "inventoryId": "<uuid>", "orderId": "<uuid>" }`
  - Trả về: `InventoryState` sau khi cancel reservation.
- `GET /api/inventory/{inventoryId}`
  - Trả về: `InventoryState` hiện tại.
- `GET /api/inventory/{inventoryId}/events?limit=50`
  - Trả về: mảng event (tương tự order events, có `eventType`, `sequenceNumber`, `timestamp`, `event` JSON).

## Ghi chú
- Kafka topic: order-service gửi request tới `inventory-service`, inventory gửi reply về `order-service`.
- Idempotency: key = `eventType:correlationId`; duplicate message sẽ bị bỏ qua, chỉ ack khi xử lý thành công.
- Event log dùng Akka Persistence Query, trả `eventType`, `sequenceNumber`, `timestamp`, `event` JSON.

## Mock Payment service (port 8083)
- DB: PostgreSQL (localhost:3001/mock-payment) – docker-compose kèm Postgres có sẵn.
- Kafka: dùng bootstrap localhost:9092 (cùng cluster với các service khác).
- Nhận `PaymentRequested` (topic `payment-service`), tạo invoice PENDING, expiresAt = now + `payment.expiry-seconds` (cấu hình trong `mock-payment/src/main/resources/application.yml`).
- API:
  - `POST /payments/pay`  
    Body: `{ "orderId": "<uuid>" }` → mark SUCCESS, emit `PaymentResult` SUCCESS → topic `order-service`.
  - `POST /payments/config/timeout`  
    Body: `{ "seconds": <long> }` → cập nhật TTL cho invoice mới.
  - `GET /payments/{orderId}` → trả invoice JSON:  
    `{ "orderId", "userId", "amount", "status": "PENDING|SUCCESS|FAILED", "correlationId", "createdAt", "expiresAt" }`
- Scheduler (5s) tự hết hạn invoice PENDING → mark FAILED, emit `PaymentResult` FAILED (reason TIMEOUT) → topic `order-service`.
