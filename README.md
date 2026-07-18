# 🧑‍💼 Employee Data Service

A backend microservice for managing employee records, built with Spring Boot. This service provides a REST API to create and retrieve employees while strictly safeguarding sensitive personal data (Social Security Numbers).

## 📋 Prerequisites
- **Java 17** or higher ☕
- **Maven** (or use the wrapper `./mvnw`) 🪶
- **Docker** and **Docker Compose** (for the local database) 🐳

## 🚀 How to Run Locally

1. **Start the database:** 🗄️
   The project uses PostgreSQL for local execution. Start it using Docker Compose:
   ```bash
   docker-compose up -d
   ```
2. **Run the application:** ▶️
   ```bash
   ./mvnw spring-boot:run
   ```
   The service will start on `http://localhost:8080`.

3. **Stop and clean up:** 🧹
   When you are done, you can stop the database container and remove the associated networks, volumes, and images by running:
   ```bash
   docker-compose down -v --rmi all
   ```

The -v flag removes the attached volumes (so the database 'forgets' its data), while --rmi all takes care of deleting the downloaded images. If you'd rather keep the image on your drive for future use, just leave out the --rmi all part.


## 💻 How to Work with the API

You can use the provided `requests.http` file in your IDE to interact with the API, or use `curl`:

**Create an Employee:** ➕
```bash
curl -X POST http://localhost:8080/employees \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Ada",
    "lastName": "Lovelace",
    "dateOfBirth": "1990-01-01",
    "gender": "FEMALE",
    "socialSecurityNumber": "123-45-6789"
  }'
```

**List Employees:** 📃
```bash
curl http://localhost:8080/employees?page=0&size=20
```

## 🧪 How to Run Tests

The application uses an in-memory H2 database for testing, so no external infrastructure is required.
Run the test suite using:
```bash
./mvnw test
```

## 🛠️ Technology Choices & Rationale

- **Framework:** Spring Boot 3.3.4 & Java 17. Chosen for rapid development, robust validation (JSR-380), and excellent ecosystem support.
- **Database:** PostgreSQL (via Docker) for local/prod simulation, H2 for isolated, fast unit/integration testing.
- **Sensitive Data Handling (Hashing vs. Encryption):** 🔐
  - **Approach:** SSNs are hashed using a one-way **HMAC-SHA256** with a server-side secret key before being persisted. The raw SSN is never stored, logged, or returned.
  - **Why Hashing:** The system only needs to know "Does this SSN already exist?", not "What is the SSN?". One-way hashing fits perfectly for equality checks.
  - **Why HMAC-SHA256 over plain SHA-256:** SSNs have a small, predictable value space (~10^9 possibilities). A plain hash is vulnerable to precomputed rainbow tables/brute force. Keying the hash with a server secret defeats this.
  - **Why not AES Encryption:** Reversibility is a liability. If a database and encryption key are compromised, all SSNs are exposed. Since this service never needs to output the real SSN, we eliminate that risk entirely.
  - **Why not BCrypt/Argon2:** Password hashers generate a random salt per record. This would make checking for duplicates an O(N) operation (fetching all records to compare). A deterministic keyed HMAC allows the database to enforce uniqueness directly via a unique index constraint.

## ⏳ What I'd Do Differently With More Time

1. **Database Migrations:** 🏗️ Replace `spring.jpa.hibernate.ddl-auto=update` with a proper schema migration tool like Flyway or Liquibase.
2. **Secrets Management:** 🤫 Move the `app.ssn.hmac-secret` out of application properties/environment variables and into a dedicated secrets manager (e.g., AWS Secrets Manager, HashiCorp Vault).
3. **API Documentation:** 📖 Integrate Springdoc OpenAPI / Swagger UI to auto-generate interactive API documentation.
4. **Enhanced Filtering:** 🔍 Add filtering capabilities to the `GET /employees` endpoint (e.g., filter by last name or date of birth).

## 🤖 AI Tool Usage

- **Tools Used:** Claude and Gemini.
- **What they were used for:** I used **Claude** to generate the initial source files and boilerplate code. Afterward, I manually assembled the generated code into a cohesive, working Spring Boot project and independently verified that the logic strictly complied with all requirements from the assignment PDF. Finally, I used **Gemini** to double-check my compliance verification and to generate this `README.md` file according to my specific instructions.
- **Changed/Rejected Suggestion:** 🛑 During the initial logic generation, Claude suggested using **BCrypt** for storing the SSNs. I actively rejected this approach. While BCrypt is an industry standard for passwords, its random per-record salt makes it impossible to enforce database-level uniqueness via an index. I opted for a deterministic HMAC-SHA256 approach instead, ensuring O(1) duplicate checks via the DB unique constraint while still preventing brute-force attacks via the server-side secret.
