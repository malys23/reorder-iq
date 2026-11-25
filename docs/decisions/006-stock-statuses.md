# 6. Stock statuses
Date: 2026-11-25
Status: Accepted

## Context
ADR 4 defines "below ROP" and "excess" but a stocked out item is also below ROP, thus the categories overlap.

## Decision
**Status:** will be assigned and should be mutually exclusive with a priority order:
| Priority | Status | Rule |
| --- | --- | --- |
| 1 | `STOCKOUT` | on hand is 0 |
| 2 | `BELOW_REORDER_POINT` | position strictly below ROP |
| 3 | `EXCESS` | position above ROP + 90 × d |
| 4 | `OK` | everything else |
**Stock out:** looks at on hand stock only
**Excess stock:** defined as (position − (ROP + 90 × d)) × unit cost, in units above the threshold, summed over the excess items
**Boundaries:**: a position at exactly ROP is not below it, and a position at exactly the excess threshold is not excess.
**Insufficient history:** an item with no demand history and stock on hand is reported as EXCESS, with its whole stock as the excess quantity. The insufficient history flag will let the planner know why

## Consequences
- Items will have clear distinctions between stock out and items below rop that need to be ordered
- Excess stock will be reported for items with insufficient history
- Statuses are computed from the data on demand and not stored, so they always match the current stock and demand.
- The four counts add up to the total number of items