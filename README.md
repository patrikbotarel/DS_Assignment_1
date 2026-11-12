# Energy Management System - Distributed Architecture

Acest proiect reprezintă o aplicație distribuită pentru managementul energiei, structurată pe o arhitectură de tip microservicii. Sistemul gestionează utilizatori, dispozitive inteligente și monitorizarea consumului de energie.

## 🏗️ Arhitectura Sistemului

Sistemul este compus din următoarele componente rulate în containere Docker izolate:

1. **Traefik (API Gateway) - Port 80**
   - Punctul unic de intrare (Single Entry Point) pentru toate cererile clientului.
   - Rutează traficul către microserviciile din spate.
   - Interceptează cererile și validează token-urile JWT folosind mecanismul ForwardAuth.

2. **Auth Service - Port 8082**
   - Gestionează autentificarea utilizatorilor și generarea token-urilor JWT.
   - Implementează Role-Based Access Control (RBAC) pentru rolurile de ADMIN și CLIENT.
   - Bază de date proprie: `auth_db` (MySQL).

3. **User Service - Port 8080**
   - Gestionează operațiunile CRUD pentru entitățile de tip Utilizator.
   - Sincronizează datele de autentificare cu Auth Service printr-o comunicare HTTP sincronă (POST).
   - Bază de date proprie: `energy_db` (MySQL).

4. **Device Service - Port 8081**
   - Gestionează dispozitivele inteligente și asocierea acestora cu utilizatorii (One-to-Many mapat prin UUID).
   - Bază de date partajată logic: `energy_db` (MySQL).

5. **Frontend (React SPA)**
   - Interfața utilizator care comunică exclusiv prin API Gateway.

## 🚀 Tehnologii Utilizate
* **Backend:** Java 17, Spring Boot 3, Spring Security, Hibernate
* **Database:** MySQL 8
* **Security:** JSON Web Tokens (JWT)
* **Infrastructură & DevOps:** Docker, Docker Compose, Traefik
* **Frontend:** React.js

## ⚙️ Cum se rulează proiectul

Proiectul este complet containerizat și poate fi pornit folosind Docker. Asigurați-vă că porturile 80, 8080, 8081, 8082 și 3306 sunt libere pe mașina host.

1. Deschideți un terminal în folderul principal al proiectului.
2. Rulați următoarea comandă pentru a construi imaginile și a porni sistemul în background:

   ```bash
   docker compose up --build -d