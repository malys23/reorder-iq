# 3. Reorder point definition

Date: 2026-11-04
Status: Accepted

## Context
The reorder point is calculated for both the data generator and in the Spring Boot backend - both must match.

## Decision
- I use a window of the last 26 weeks
- z values organized by classes ABC (2.05, 1.645, 1.28)
- Daily figures are derived from weekly ones and sample standard deviation
- Items are flagged on inventory positions - on_hand and on_order
- Boundary rule which flags when a position is below, or at or below ROP
- Excess means more than ROP plus about 90 days of demand
The point of reference is always the backend.

## Consequences
- The data is consistent with the application and the backend test can compare against the generator's numbers
- There are two implementations to keep in step, and parameters that must be changed in both