import numpy as np
from config import STATS_WEEKS, Z_BY_CLASS, STOCK_SITUATIONS, NUM_ITEMS
from utils import build_counted_list

#Returns (d, sigma_d): daily demand mean and daily demand standard deviation
def demand_stats(weekly_demand):
    recent = weekly_demand[-STATS_WEEKS:]
    d = recent.mean() / 7
    sigma_d = recent.std(ddof=1) / np.sqrt(7) 
    return d, sigma_d

#Returns reorder point and safety stock
def reorder_point(d, sigma_d, lead_time_days, z):
    safety_stock = z * sigma_d * np.sqrt(lead_time_days)
    rop = d * lead_time_days + safety_stock
    return rop, safety_stock

#Returns how much stock is on hand for a specific siuation
def draw_on_hand(situation, rop, d, rng):
    on_hand = 0
    cfg = STOCK_SITUATIONS[situation]
    if cfg["kind"] == "zero":
        return on_hand
    low, high = cfg["range"]
    x = rng.uniform(low, high)
    if cfg["kind"] == "rop_multiple":
        on_hand = int(rop * x)
        return on_hand 
    on_hand = int(rop + d*x)
    return on_hand

#Returns stock levels
def generate_stock_levels(items, demand_by_item, rng):
    situations = build_counted_list(STOCK_SITUATIONS, NUM_ITEMS, rng)
    stock= []
    for item, situation in zip(items, situations):
        d, sigma_d = demand_stats(demand_by_item[item["item_id"]])
        z = Z_BY_CLASS[item["abc_class"]]
        rop, _ = reorder_point(d, sigma_d, item["lead_time_days"], z)
        on_hand = draw_on_hand(situation, rop, d, rng)

    
    return stock
