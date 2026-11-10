# ReorderIQ backend
 
Spring Boot service for ReorderIQ, a procurement and inventory planning tool. It stores items, stock, demand history and purchase requests in PostgreSQL, and will expose the reorder calculation and the purchase request workflow through a REST API.
 
All data is synthetic. See [`data/README.md`](../data/README.md) for how it is generated.
 
## Status
 
Phase 2 (backend) is in progress.
 
| Area | State |
| --- | --- |
| Database schema (Flyway) | Done |
| JPA entities and repositories, with Testcontainers tests | Done |
| Development seed loader | Done |
| Reorder calculation service | Planned |
| Items and dashboard endpoints | Planned |
| Login with JWT and role rules | Planned |
| Purchase request workflow | Planned |
| API documentation (Swagger UI) | Planned |
 
Until the login work is done, the default Spring Security setup is active and generates a throwaway password at startup. It is a placeholder, not the final security design.
 
## Stack
 
- Java 21, Spring Boot 4.1
- PostgreSQL 17
- Flyway for schema migrations
- Spring Data JPA (Hibernate) with `ddl-auto: validate`
- Apache Commons CSV and `JdbcTemplate` for the development seed loader
- JUnit 5 and Testcontainers for repository tests

## Running it locally
 
You need Java 21, Maven and Docker.
 
1. Start the database. From the folder that contains `docker-compose.yml`:
```
   docker compose up -d
```
 
2. Start the app: run `ReorderiqApiApplication` from your IDE, or from `backend/` in a terminal:
```
   mvn spring-boot:run
```
 
   The app must start from the `backend/` folder, because the seed folder is configured as the relative path `../data/output`. On the first start, Flyway creates the schema and the seed loader fills the database (see below).
 
Database settings can be overridden with the environment variables `DB_USER` and `DB_PASSWORD`. The defaults in `application-dev.yaml` are for the local Docker database only.
 
## Profiles
 
| Profile | Used for | Notes |
| --- | --- | --- |
| `dev` | Local development | The default profile. Holds the local datasource defaults, the seed settings and the demo users. |
 
Settings shared by every profile (`ddl-auto: validate`, the default profile) are in `application.yaml`. A production profile with no fallback credentials is planned.
 
## Database
 
The schema is defined by versioned Flyway migrations in `src/main/resources/db/migration/`, and Hibernate only validates the entities against it. A migration that has already run is never edited. Changes go in a new `V2__...sql`.
 
Tables: `users`, `suppliers`, `items`, `stock_levels`, `demand_history`, `purchase_requests` and `forecasts`.
 
## Seed data (development)
 
In the `dev` profile, the app fills the database from the generated CSV files in `data/output/` the first time it starts.
 
- **What is loaded:** 3 demo users, 17 suppliers, 300 items, 300 stock levels, 31,200 weekly demand rows and 142 purchase requests. The last 13 weeks of each item's demand are flagged `is_holdout`.
- **Safe to restart:** if items already exist, the loader logs "Seed data already present, skipping" and does nothing. The load runs in a single transaction, so a failure leaves the database empty.
- **Reset:** `docker compose down -v`, then `docker compose up -d` and start the app.
- **Settings:** the data folder (a relative path) and the demo users are in `src/main/resources/application-dev.yaml`. The loader does not run in any other profile.

### Demo accounts
 
These exist only in the local development database. They are not real credentials.
 
| Username | Role | Password |
| --- | --- | --- |
| `demo-planner` | planner | demo-planner-pw |
| `demo-approver` | approver | demo-approver-pw |
| `demo-receiver` | receiver | demo-receiver-pw |
 
The user ids (1, 2, 3) match the ids used in the generated purchase request data.
 
## Tests
 
```
mvn test
```
 
The repository tests start a PostgreSQL container with Testcontainers, apply the real Flyway migration to it, and check that items, suppliers, stock levels and demand history can be saved and read back, including the "last N weeks of demand" query. **Docker must be running.**
 
## Project layout
 
```
src/main/java/io/github/malys23/reorderiq_api/
├── domain/       JPA entities (one per table)
├── repository/   Spring Data repositories
├── seed/         Development-only seed loader
└── config/       Spring configuration
src/main/resources/
├── db/migration/ Flyway migrations
└── application*.yaml
```