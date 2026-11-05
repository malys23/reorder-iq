import numpy as np
import math
from config import STATS_WEEKS, Z_BY_CLASS, ON_HAND_RULES, ON_ORDER_RULES, NUM_ITEMS
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
    rule = ON_HAND_RULES[situation]
    if rule["kind"] == "zero":
        return on_hand
    low, high = rule["range"]
    x = rng.uniform(low, high)
    if rule["kind"] == "rop_multiple":
        on_hand = math.ceil(rop * x)
        return on_hand 
    on_hand = math.ceil(rop + d*x)
    return on_hand

#Returns how much stock is
def draw_on_order(situation, on_hand, rop, d, rng):
    rule = ON_ORDER_RULES[situation]
    if rng.random() >= rule["prob"]:
        return 0

    low, high = rule["days_range"]
    days = rng.uniform(low, high)
    if rule["kind"] == "restore_position":
        on_order = max(0, int(rop + d * days - on_hand))
        return on_order
    on_order = int(d*days)
    return on_order    

#Returns stock levels
def generate_stock_levels(items, demand_by_item, rng):
    situations = build_counted_list(ON_HAND_RULES, NUM_ITEMS, rng)
    stock= []
    reorder_info = {}
    for item, situation in zip(items, situations):
        d, sigma_d = demand_stats(demand_by_item[item["item_id"]])
        z = Z_BY_CLASS[item["abc_class"]]
        rop, _ = reorder_point(d, sigma_d, item["lead_time_days"], z)
        on_hand = draw_on_hand(situation, rop, d, rng)
        on_order = draw_on_order(situation, on_hand, rop, d, rng)
        stock.append({"item_id":item["item_id"], "on_hand": on_hand, "on_order": on_order})
        reorder_info[item["item_id"]] = {"d": d, "sigma_d": sigma_d, "rop":rop, "situation": situation}
    return stock, reorder_info
