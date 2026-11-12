# Energy Management System – Device Management Microservice

Microserviciul de **Device Management** din cadrul proiectului **Energy Management System**, realizat cu Spring Boot, JPA/Hibernate, MySQL și documentat complet cu Swagger / OpenAPI.

---

## 1. Caracteristici Implementate (Faza 2)

1. **Entitatea `Device`**:
   - `id`: UUID (identificator unic, generat automat)
   - `name`: String (obligatoriu, denumirea dispozitivului, ex. "Smart Meter Living")
   - `maxConsumption`: Double (obligatoriu, consumul maxim orar permis în kWh)
   - `description`: String (opțional, descriere sau locație)
   - `userId`: UUID (referință logică către utilizatorul căruia îi este asociat dispozitivul)

2. **Entitatea `DeviceUser`**:
   - `id`: UUID (identificatorul utilizatorului sincronizat din User Microservice)
   - Menține consistența datelor pe baza arhitecturii Database-per-Service.

3. **Operații CRUD Complete**:
   - `GET /device`: Returnează lista tuturor dispozitivelor (`List<DeviceDTO>`).
   - `GET /device/{id}`: Returnează detaliile unui dispozitiv după UUID (`DeviceDTO`).
   - `GET /device/user/{userId}`: Returnează toate dispozitivele asociate unui utilizator (folosit de rolul Client).
   - `POST /device`: Înregistrează un dispozitiv nou (validează datele și existența `userId` dacă este furnizat).
   - `PUT /device/{id}`: Actualizează proprietățile unui dispozitiv.
   - `DELETE /device/{id}`: Șterge un dispozitiv din sistem.
   - `PATCH /device/{id}/assign/{userId}`: Asociază sau reasociază un dispozitiv la un utilizator.

4. **Endpoint-uri Interne de Sincronizare (Apelate sincron de User Microservice)**:
   - `POST /device/user/{userId}`: Primește și salvează local ID-ul unui utilizator creat în User Service.
   - `DELETE /device/user/{userId}`: Șterge utilizatorul din baza locală și decuplează dispozitivele asociate acestuia (`userId = null`) pentru a preveni referințele orfane.
   - `GET /device/user`: Returnează lista tuturor ID-urilor de utilizatori sincronizați.

5. **Documentație Swagger / OpenAPI (conform cerinței din barem)**:
   - Toate endpoint-urile conțin cale, metodă HTTP, descriere (`@Operation`), parametri (`@Parameter`, `@RequestBody`) și coduri de răspuns (`@ApiResponse`: 200, 201, 204, 400, 404).
   - Swagger UI: `http://localhost:8081/swagger-ui.html`
   - OpenAPI JSON: `http://localhost:8081/v3/api-docs`

---

## 2. Configurare și Variabile de Mediu

Fișierul `.env`:
```ini
DB_NAME=device_db
DB_USER=device_user
DB_PASSWORD=device_password
DB_ROOT_PASSWORD=device_root_password
DB_PORT=3307
PORT=8081
```

---

## 3. Rulare

### Opțiunea 1: Cu Docker Compose (recomandat)

```bash
cd device-service
docker compose up --build
```

Pornește:
- Containerul MySQL 8.0 dedicat (`mysql-device-db`) pe portul gazdă `3307`
- Containerul Spring Boot (`device-service`) pe portul `8081`

### Opțiunea 2: Rulare Locală (IDE / Maven)

1. Pornește containerul MySQL al Device Service:
   ```bash
   docker compose up -d mysql-device-db
   ```
2. Pornește aplicația:
   ```bash
   mvn spring-boot:run
   ```

---

## 4. Exemple de Apeluri API (cURL)

### 4.1 Creare Dispozitiv (fără utilizator asignat inițial)
```bash
curl -X POST http://localhost:8081/device \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Smart Meter Kitchen",
    "maxConsumption": 250.0,
    "description": "Bucatarie - contor inteligent"
  }'
```

### 4.2 Sincronizare Utilizator de la User Service (apel intern)
```bash
curl -X POST http://localhost:8081/device/user/<USER_UUID>
```

### 4.3 Creare Dispozitiv asociat unui Utilizator
```bash
curl -X POST http://localhost:8081/device \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Smart Meter Living",
    "maxConsumption": 500.0,
    "description": "Living - contor principal",
    "userId": "<USER_UUID>"
  }'
```

### 4.4 Listare dispozitive după User ID (pentru Client)
```bash
curl -X GET http://localhost:8081/device/user/<USER_UUID>
```

### 4.5 Asignare dispozitiv la un utilizator
```bash
curl -X PATCH http://localhost:8081/device/<DEVICE_UUID>/assign/<USER_UUID>
```

### 4.6 Ștergere dispozitiv
```bash
curl -X DELETE http://localhost:8081/device/<DEVICE_UUID>
```
