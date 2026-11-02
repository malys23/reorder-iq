# 1. Use PostgreSQL

Date: 2026-11-02
Status: Accepted

## Context
The data is closely connected: items, suppliers, stock levels, demand history and purchase requests refer to one another. Purchase requests also move through a fixed set of statuses. The options were a relational database or a NoSQL database.

## Decision
Use PostgreSQL, a relational database, with foreign keys and constraints, and manage the schema with Flyway migrations.

## Consequences
- Relationships and valid statuses are enforced by the database, not only by application code.
- Queries that join items, stock and demand, such as the dashboard summary, are straightforward to write.
- The schema must be changed through migrations, which takes more effort than a schemaless store.
- Running it locally needs a PostgreSQL instance (provided through Docker Compose).