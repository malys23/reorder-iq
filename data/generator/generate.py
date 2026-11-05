import numpy as np
from collections import Counter
import matplotlib.pyplot as plt
from config import FIXED_SEED, COMPANY_NAMES, SUPPLIER_INFO, CATEGORIES
from suppliers import generate_suppliers
from items import generate_items
from stock import generate_stock_levels
from purchase_requests import generate_purchase_requests
from validate import validate

def main():
    rng = np.random.default_rng(FIXED_SEED)
    suppliers, suppliers_by_category = generate_suppliers(COMPANY_NAMES, SUPPLIER_INFO, CATEGORIES, rng)
    items, demand_by_item = generate_items(suppliers_by_category, rng)
    stock, reorder_info = generate_stock_levels(items, demand_by_item, rng) 
    purchase_requests = generate_purchase_requests(items, stock, suppliers, reorder_info, rng)

    #Sanity check for purchase requests
    print(Counter(i["status"] for i in purchase_requests))

    print(Counter(i["item_id"] for i in purchase_requests))

    for item in items:
        if stock[item["item_id"]-1]["on_order"]>0: 
            print(stock[item["item_id"]-1]["on_order"]==purchase_requests[item["item_id"]]["quantity"])
    
if __name__ == "__main__":
    main()
    