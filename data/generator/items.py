#main script: builds the tables and writes the CSV files
import numpy as np
from config import CATEGORIES, NUM_ITEMS, WEEKS, ABC_CUTOFFS, COMPANY_NAMES,  SUPPLIER_INFO
from demand import generate_demand 
from suppliers import generate_suppliers

#Returns list of categories, count of which predetermined with share
def build_category_list(rng):
    counts = {}
    for name, cfg in CATEGORIES.items():
        counts[name] = round(NUM_ITEMS * cfg["share"])

    diff = NUM_ITEMS - sum(counts.values())
    largest = max(counts, key=counts.get)
    counts[largest] += diff

    names = []
    for name, n in counts.items():
        for _ in range(n):
            names.append(name)

    rng.shuffle(names)
    return names

#Return random value between low and high, skewed towards low
def draw_log_uniform(low, high, rng):
    return round(float(np.exp(rng.uniform(np.log(low), np.log(high)))), 2)

#Return a list of attributes for a specific category name (item)
def draw_item_attributes(category_name, suppliers_by_category, rng):
    cfg = CATEGORIES[category_name]

    #unit cost
    low, high = cfg["unit cost"]
    unit_cost = round(draw_log_uniform(low, high, rng), 2)

    #lead time
    low, high = cfg["lead_time_days"]
    base_lead_time = int(rng.integers(low, high+1))

    options = suppliers_by_category[category_name]
    supplier = options[int(rng.integers(len(options)))]

    lead_time = max(1, round(base_lead_time * supplier["lead_time_factor"]))

    #weekly demand
    low, high = cfg["weekly_demand"]
    base_demand = int(rng.integers(low, high+1))

    #demand probability
    if cfg["pattern"] in ("intermittent", "lumpy"):
        low, high = cfg["demand_prob"]
        demand_prob = round(float(rng.uniform(low, high)), 2)
    else:
        demand_prob = 1.0

    #trend_pct_per_year
    low, high = cfg["trend_pct_per_year"]
    trend_pct_per_year = round(float(rng.uniform(low, high)),1)

    #season strength and peak week:
    if cfg["pattern"] == "seasonal":
        low, high = cfg["season_strength"]
        season_strength = round(float(rng.uniform(low,high)), 2)
        peak_week = cfg["peak_week"] + int(rng.integers(-3,4))
    else:
        season_strength = 0.0
        peak_week: 0.0

    return{
        "category": category_name,
        "supplier_id": supplier["supplier_id"],
        "supplier_name": supplier["name"],
        "unit_cost": unit_cost,
        "lead_time_days": lead_time,
        "base_demand": base_demand,
        "demand_prob": demand_prob,
        "pattern": cfg["pattern"],
        "trend_pct_per_year": trend_pct_per_year,
        "season_strength": season_strength,
        "peak_week": peak_week
    }

#Returns a complete item and its demand
def make_item(item_id, category_name, rng):
    attrs = draw_item_attributes(category_name, rng)
    demand = generate_demand(attrs, WEEKS, rng)

    item = {"item_id": item_id, **attrs}
    item["annual_units"] = int(demand[-52:].sum())
    return item, demand

#Does not return anything, just modifies the item, adding class
def assign_abc_classes(items):
    a_cut, b_cut = ABC_CUTOFFS
    for item in items:
        item["annual_value"] = item["unit_cost"] * item["annual_units"]

    ranked = sorted(items, key=lambda it: it["annual_value"], reverse = True)
    total = sum(it["annual_value"] for it in ranked)

    running = 0.0
    for item in ranked:
        share_before = running/ total
        running += item["annual_value"]
        if share_before < a_cut:
            item["abc_class"] = "A"
        elif share_before < b_cut:
            item["abc_class"] = "B"
        else:
            item["abc_class"] = "C"

def generate_items(rng):
    categories = build_category_list(rng)

    items = []
    demand_by_item = []
    for item_id, category_name in enumerate(categories, start=1):
        item, demand = make_item(item_id, category_name, rng)
        items.append(item)
        demand_by_item[item_id] = demand

    assign_abc_classes(items)

    return items, demand_by_item