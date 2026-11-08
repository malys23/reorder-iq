import numpy as np
import matplotlib.pyplot as plt
from collections import Counter, defaultdict
from check_items import check_items
from check_suppliers import check_suppliers
from check_demand import check_demand
from check_stock import check_stock
from check_prs import check_purchase_requests
from config import AS_OF, NUM_ITEMS, CATEGORIES, WEEK_START, WEEKS, HOLDOUT_WEEKS

#Keeping some validation code to later format for possible testing

def validate(items, suppliers, demand_by_item, final_demand, stock, reorder_info, purchase_requests):
    #Validation for items
    check_items(items, suppliers, CATEGORIES, NUM_ITEMS, show=5)

    #Validation for suppliers
    check_suppliers(suppliers, CATEGORIES, show=5)

    #Validation for demand
    check_demand(items, final_demand, WEEKS, WEEK_START, HOLDOUT_WEEKS, show=5)

    #Validation for stock
    check_stock(items, stock, reorder_info, show=5)

    #Validation for prs
    check_purchase_requests(items, stock, purchase_requests, AS_OF, show=5)

    #Demand checks with plots
    examples = {}
    for item in items:
        examples.setdefault(item["pattern"], item)

    fig, axes = plt.subplots(2, 2, figsize=(11, 7), sharex=True)
    for ax, (pattern, item) in zip(axes.flat, examples.items()):
        ax.plot(demand_by_item[item["item_id"]])
        ax.set_title(f'{pattern}: {item["category"]} (item {item["item_id"]})')
        ax.set_xlabel("week")
        ax.set_ylabel("units")
    plt.tight_layout()
    plt.show()