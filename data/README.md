# ReorderIQ synthetic data

This folder holds the generator that creates the dataset for ReorderIQ, and the CSV files it produces. All data is synthetic. It is meant to resemble the inventory and purchasing data of an industrial supply operation (fasteners, valves, pipe and similar items), but none of it comes from a real company.

## What it generates

| File | One row per | Rows |
| --- | --- | --- |
| `suppliers.csv` | supplier | 17 |
| `items.csv` | item | 300 |
| `stock_levels.csv` | item | 300 |
| `demand_history.csv` | item per week | 31,200 (300 items × 104 weeks) |
| `purchase_requests.csv` | purchase request | 142 |

Columns:
- **suppliers:** `supplier_id`, `name`, `category`, `lead_time_cv`, `on_time_rate`
- **items:** `item_id`, `name`, `category`, `supplier_id`, `unit_cost`, `lead_time_days`, `abc_class`
- **stock_levels:** `item_id`, `on_hand`, `on_order`
- **demand_history:** `item_id`, `week_start`, `quantity`, `is_holdout`
- **purchase_requests:** `request_id`, `item_id`, `quantity`, `needed_by`, `created_at`, `requested_by`, `status`, `submitted_at`, `approved_at`, `approved_by`, `ordered_at`, `received_at`, `received_by`

Dates are written as `YYYY-MM-DD`. A date that has not happened yet, such as `received_at` on an open request, is an empty cell. For demand history, `week_start` is always a Monday and `is_holdout` is marked 1 for items to include in the holdout and 0 for the rest.

## How to run it

From this folder's `generator/` directory:

```
python -m uv run generate.py
```

The script generates the data, runs the checks (below), and writes the CSV files to `data/output/`. All settings (item count, seed, category parameters, stock and request rules) are in `generator/config.py`.

The random seed is fixed, so repeated runs produce identical files. 

## How the data is built

**Categories.** Items are split across eight categories. The share is the proportion of items, and the pattern is how weekly demand behaves.

| Category | Share | Demand pattern |
| --- | --- | --- |
| Fasteners | 20% | smooth |
| Consumables | 25% | smooth |
| Gaskets and seals | 7% | seasonal |
| Fittings and flanges | 7% | seasonal |
| Pipe | 11% | seasonal |
| Valves | 10% | intermittent |
| Instrumentation | 10% | intermittent |
| Inserts | 10% | lumpy |

**Inserts refer to piping inserts used in industrial oil pipe production.

**Demand.** Each item has 104 weeks of history from Monday 11/04/2024 to the week starting Monday 11/02/2026. The patterns are simple parametric models:
- **Smooth:** a base level with about 10% random variation.
- **Seasonal:** the smooth pattern multiplied by a yearly cosine wave. Each category peaks in a different week, with a small shift per item.
- **Intermittent:** most weeks are zero. In the weeks that have demand, the quantity is small and fairly consistent.
- **Lumpy:** most weeks are zero, and the quantities vary a lot when demand does occur.

Every item also gets a small yearly trend (growth or decline). The parameters were chosen by hand to give plausible patterns. They were not fitted to real data.

**Unit cost** is drawn log-uniformly within a range for each category, so most items are cheap and a few are expensive.

**ABC class.** Items are ranked by annual value (unit cost × units sold in the last 52 weeks). Class A is the items that make up the first 80% of the value, B the next 15%, and C the rest. The class sets the service level used for safety stock.

**Suppliers.** Each category has between one and three suppliers, and each supplier serves one category. Every item is assigned one supplier from its category. A supplier has a speed factor, a lead-time variability (`lead_time_cv`) and an on-time rate. The item's lead time is the category's base lead time scaled by its supplier's speed factor.

**Reorder point.** The generator calculates a reorder point for each item, to create realistic stock levels:

ROP = d × L + z × σ<sub>d</sub> × √L

where *d* is average daily demand over the last 26 weeks, *σ<sub>d</sub>* is the daily standard deviation, *L* is the lead time in days, and *z* depends on the ABC class (A: 2.05, B: 1.645, C: 1.28). **The backend is the reference implementation.** The generator's copy only needs to be close enough to create the situations below.

**Stock levels.** Each item is assigned a starting situation as of 11/02/2026:

| Situation | Share | On hand |
| --- | --- | --- |
| Stockout | 5% | 0 |
| Below reorder point | 20% | between 20% and 100% of the reorder point |
| Healthy | 60% | from the reorder point up to the reorder point plus 45 days of demand |
| Excess | 15% | the reorder point plus 100 to 200 days of demand |

Some items already have an order on the way (`on_order`), more often among the stockout and below-ROP items. An open order is sized to bring the stock position back above the reorder point.

**Purchase requests.** Requests move through five statuses: draft, submitted, approved, ordered, received.

- Every item with `on_order > 0` has exactly one *ordered* request, with the same quantity.
- Items that need ordering and have no open order get a draft, submitted or approved request most of the time, and no request otherwise.
- A share of healthy and excess items have a *received* request in the recent past.
- Three user ids are used: planner (1), approver (2) and receiver (3). The backend must create users with the same ids.
- Dates are built backwards from a fixed "as of" date. For received requests, the delivery time varies around the supplier's lead time, using the supplier's on-time rate.
- `needed_by` is the creation date plus the lead time plus a two-day processing allowance. It is a target date, not a deadline set by a customer.

## Holdout
The holdout starts 8/3/2026 on and covers the last 13 weeks (weeks 92 to 104) of every item's demand history. Items flagged for holdout will be used for testing and evaluation of the chosen method in the forecasting segment of this project. They must not be used for training, tuning, model selection or parameter choosing. However the reorder point and stock levels are snapshots of the last week and still use all history. The holdout applies to forecasting models only, not the reorder calculations. The ABC class, trend-related attributes, and reorder point are all calculated from the entire history so forecasting models shouldn't use them as input features.

## Checks

`validate.py` and the check files verify the generated data before any file is written. Examples: every item with an open order has a matching *ordered* request with the same quantity, stock levels match their situation (stockouts at zero, below-ROP items under the reorder point), dates are in order and none is in the future, and quantities are positive whole numbers, the demand history is complete and in weekly order, the start date is a Monday, exactly 13 weeks per item are flagged as holdout, and the catalog checks on items and suppliers (valid supplier and class, positive costs and lead times).

## Limitations

- The demand patterns are assumptions. Results from forecasting experiments on this data show how methods behave on these patterns, not how they would perform on real data.
- Each item has one supplier, and each supplier serves one category.
- Stock is a single snapshot at the "as of" date. There is no stock history over time.
- Reorder point, safety stock and excess stock are defined in the backend. If the generator and backend disagree at the edges (an item within one unit of its reorder point, for example), the backend's answer is the correct one.