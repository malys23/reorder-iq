# 5. Suggested order quantity

Date: 2026-11-24
Status: Accepted

## Context
ADR 4 defines when to order (the reorder point) but not how much thus this ADR covers the suggested order quantity. This builds on ADR 4.

## Decision
- **Order-up-to level:** ROP + demand over the order cover period
- **Order cover:** order-cover-weeks is a business parameter stored in configuration and set to a default of 3 weeks. This number was chosen to reflect lean operation, short lead times, and near-daily checks by the planner
- **Suggested quantity:** order-up-to - (on hand + on order) and is rounded up to the tolerance. It is never below the minimum order quantity, and is set to 0 when the position is not strinctly below the ROP.
- **Minimum order quantity by unit cost tiers;** three tiers were created for each order quantity bracket and live in the configuration file:
    - unit cost below $10           min order quantity: 100
    - unit cost $10 to below $100   min order quantity: 50
    - unit cost $100 and more       min order quantity: 1
- **Assumptions:** near daily stock checks by the planner, expediting will be handled by the planner and added later as an application feature, planning manager will provide timely pr approvals, and safety stock covers the lead time only

## Consequences
- Cheap, slow items will often order the minimum of 100, which may look like excess under the 90-day rule. This is expected, not a bug.
- The cover weeks and the tiers are tuning parameters, and the synthetic data can be used to compare settings.
- The backend is the reference implementation, so if the generator ever suggests quantities, it must use these rules
