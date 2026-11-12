# Energy Management System – User Management Microservice

Microserviciul de **User Management** din cadrul proiectului **Energy Management System**, realizat cu Spring Boot, JPA/Hibernate, MySQL și documentat complet cu Swagger / OpenAPI.

---

## 1. Caracteristici Implementate (Faza 1)

1. **Entitatea `User`**:
   - `id`: UUID (identificator unic, generat automat)
   - `username`: String (unic, obligatoriu)
   - `password`: String (obligatoriu)
   - `name`: String (numele complet al utilizatorului)
   - `role`: Enum `UserRole` (`ADMIN`, `CLIENT` / `Admin`, `Client`)

2. **Operații CRUD Complete**:
   - `GET /user`: Returnează lista tuturor utilizatorilor (`List<UserDTO>`).
   - `GET /user/{id}`: Returnează detaliile unui utilizator pe baza ID-ului său UUID (`UserDetailsDTO`).
   - `POST /user`: Creează un nou utilizator, returnează ID-ul creat și sincronizează apelul sincron către Device Microservice.
   - `PUT /user/{id}`: Actualizează datele utilizatorului (username, name, password, role).
   - `DELETE /user/{id}`: Șterge utilizatorul și notifică Device Microservice printr-un apel sincron DELETE pentru a menține consistența.

3. **Sincronizare HTTP Sincronă cu Device Microservice**:
   - Implementată prin `DeviceServiceClient` folosind `RestTemplate`.
   - La creare (`POST /user`), microserviciul trimite `POST {DEVICE_SERVICE_URL}/user/{userId}`.
   - La ștergere (`DELETE /user/{id}`), microserviciul trimite `DELETE {DEVICE_SERVICE_URL}/user/{userId}`.
   - Endpoint-ul și starea sunt configurabile prin proprietățile:
     - `device.service.url` (default: `http://localhost:8081/device`)
     - `device.service.sync.enabled` (default: `true`)
   - Dacă Device Microservice nu este pornit încă (aflat în faza de dezvoltare/testare izolată), apelul eșuează silențios cu log de avertizare explicit (`WARN`), permițând testarea izolată a Fazei 1 fără erori 500.

4. **Documentație Swagger / OpenAPI (conform cerinței din barem)**:
   - Toate endpoint-urile conțin:
     - Cale (Path)
     - Metodă HTTP (`GET`, `POST`, `PUT`, `DELETE`)
     - Descriere scurtă (`@Operation`)
     - Parametri de intrare (`@Parameter`, `@RequestBody`)
     - Răspunsuri posibile (`@ApiResponse`: 200, 201, 204, 400, 404, 409)
   - Interfața Swagger UI este disponibilă la:
     - `http://localhost:8080/swagger-ui.html`
     - `http://localhost:8080/swagger-ui/index.html`
   - Specificația OpenAPI JSON:
     - `http://localhost:8080/v3/api-docs`

---

## 2. Configurare și Variabile de Mediu

Fișierul `.env` este citit automat atât de Spring Boot cât și de Docker Compose:

```ini
DB_NAME=energy_db
DB_USER=energy_user
DB_PASSWORD=energy_password
DB_ROOT_PASSWORD=root_password
DB_PORT=3306
DEVICE_SERVICE_URL=http://localhost:8081/device
DEVICE_SERVICE_SYNC_ENABLED=true
```

---

## 3. Rulare

### Opțiunea 1: Cu Docker Compose

```bash
docker compose up --build
```

Acest lucru pornește:
- Containerul MySQL 8.4 (`mysql-db`)
- Containerul Spring Boot (`spring-demo` / user-service) pe portul `8080`

### Opțiunea 2: Rulare Locală (IDE / Maven)

1. Pornește baza de date MySQL (local sau prin docker):
   ```bash
   docker compose up -d mysql-db
   ```
2. Rulează aplicația:
   ```bash
   mvn spring-boot:run
   ```

---

## 4. Exemple de Apeluri API (cURL)

### 4.1 Creare Utilizator (Admin)
```bash
curl -X POST http://localhost:8080/user \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "adminPassword123!",
    "name": "System Administrator",
    "role": "ADMIN"
  }'
```
*Răspuns:* `201 Created` cu UUID-ul utilizatorului creat.

### 4.2 Creare Utilizator (Client)
```bash
curl -X POST http://localhost:8080/user \
  -H "Content-Type: application/json" \
  -d '{
    "username": "client1",
    "password": "clientPassword123!",
    "name": "Popescu Ion",
    "role": "CLIENT"
  }'
```

### 4.3 Preluare toți utilizatorii
```bash
curl -X GET http://localhost:8080/user
```

### 4.4 Preluare utilizator după ID
```bash
curl -X GET http://localhost:8080/user/<UUID>
```

### 4.5 Actualizare utilizator
```bash
curl -X PUT http://localhost:8080/user/<UUID> \
  -H "Content-Type: application/json" \
  -d '{
    "username": "client1_modificat",
    "name": "Popescu Ion Vasile",
    "role": "CLIENT"
  }'
```

### 4.6 Ștergere utilizator
```bash
curl -X DELETE http://localhost:8080/user/<UUID>
```
*Răspuns:* `204 No Content`. În fundal, se trimite apel sincron `DELETE http://localhost:8081/device/user/<UUID>` către Device Microservice.
