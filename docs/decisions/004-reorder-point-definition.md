# 3. Reorder point definition

Date: 2026-11-04
Updated: 2026-11-23
Status: Accepted

## Context
The reorder point is calculated for both the data generator and in the Spring Boot backend - both must match.

## Decision
- **Window:** the last 26 weeks of weekly demand, including the holdout weeks. The holdout rule applies to forecasting models only, not to this calculation. This value is kept in configuration under reorderiq.reorder in application.yaml and not as constants in the code.
- **Daily figures from weekly ones:** d = weekly mean ÷ 7 and σ_d = weekly sample standard deviation (n − 1) ÷ √7.
- **Independence Assumption:** daily demand is independent from day to day, so variances add up and the weekly variance is 7 times the daily one. Hence σ_daily = σ_weekly / √7.
- **Formulas:** safety stock SS = z × σ_d × √L, and reorder point ROP = d × L + SS, with L the lead time in days.
- **z by ABC class:** A 2.05 (98%), B 1.645 (95%), C 1.28 (90%), also kept in configuration.
- **Inventory position:** on_hand + on_order. An item is flagged below the reorder point when its position is strictly below ROP.
- **Excess:** inventory position above ROP plus 90 days of demand (90 × d).
- **Edge Cases:** no history gives zeros and insufficientHistory flag, one week gives a standard deviation of 0, and an unknown ABC class is rejected.
- **Reference:** the backend is always the point of reference
- **Rounding:** results are not rounded in the calculation - rounding for display purposes will happen later to prevent disagreement by unit.

## Consequences
- The data is consistent with the application and the backend test can compare against the generator's numbers
- There are two implementations to keep in step, and parameters that must be changed in both
- σ_daily is an estimate built from weekly data as if demand is correlated from day to day data, the safety stock will be too low