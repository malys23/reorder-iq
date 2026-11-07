from collections import defaultdict
from datetime import timedelta
from utils import _report

#demand_rows: the list of {item_id, week_start, quantity} rows (the output form).
def check_demand(items, demand_rows, weeks, start_date, show=5):
    failures = defaultdict(list)
    item_ids = {i["item_id"] for i in items}
 
    if len(demand_rows) != len(items) * weeks:
        failures["row count != items x weeks"].append(len(demand_rows))
    if start_date.weekday() != 0:
        failures["start date is not a Monday"].append(str(start_date))
 
    by_item = defaultdict(list)
    for row in demand_rows:
        by_item[row["item_id"]].append(row)
        q = row["quantity"]
        if q < 0 or q != int(q):
            failures["quantity negative or not whole"].append(row["item_id"])
 
    failures["demand for unknown item"] = [i for i in by_item if i not in item_ids]
    failures["item without demand rows"] = [i for i in item_ids if i not in by_item]
 
    for item_id, rows in by_item.items():
        if len(rows) != weeks:
            failures["item with wrong number of weeks"].append(item_id)
            continue
        dates = [r["week_start"] for r in rows]
        if dates[0] != start_date:
            failures["history does not start at start_date"].append(item_id)
        if any(b - a != timedelta(weeks=1) for a, b in zip(dates, dates[1:])):
            failures["weeks not consecutive"].append(item_id)
 
    failures = {k: sorted(set(v)) if k != "row count != items x weeks" else v
                for k, v in failures.items() if v}
    return _report("Demand checks", f"{len(demand_rows)} rows for {len(by_item)} items", failures, show)