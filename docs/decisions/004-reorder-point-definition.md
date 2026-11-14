# 3. Reorder point definition

Date: 2026-11-04
Updated: 2026-11-14
Status: Accepted

## Context
The reorder point is calculated for both the data generator and in the Spring Boot backend - both must match.

## Decision
- **Window:** the last 26 weeks of weekly demand, including the holdout weeks. The holdout rule applies to forecasting models only, not to this calculation.
- **Daily figures from weekly ones:** d = weekly mean ÷ 7 and σ_d = weekly sample standard deviation (n − 1) ÷ √7.
- **Formulas:** safety stock SS = z × σ_d × √L, and reorder point ROP = d × L + SS, with L the lead time in days.
- **z by ABC class:** A 2.05 (98%), B 1.645 (95%), C 1.28 (90%), kept in configuration.
- **Inventory position:** on_hand + on_order. An item is flagged below the reorder point when its position is strictly below ROP.
- **Excess:** inventory position above ROP plus 90 days of demand (90 × d).
- **Reference:** the backend is always the point of reference.

## Consequences
- The data is consistent with the application and the backend test can compare against the generator's numbers
- There are two implementations to keep in step, and parameters that must be changed in both