import numpy as np
from datetime import timedelta
from config import FIXED_SEED, COMPANY_NAMES, SUPPLIER_INFO, CATEGORIES, COLUMN_NAMES, WEEK_START
from suppliers import generate_suppliers
from items import generate_items
from stock import generate_stock_levels
from purchase_requests import generate_purchase_requests
from validate import validate
from utils import generate_formatted_csv

def main():
    rng = np.random.default_rng(FIXED_SEED)
    suppliers, suppliers_by_category = generate_suppliers(COMPANY_NAMES, SUPPLIER_INFO, CATEGORIES, rng)
    items, demand_by_item = generate_items(suppliers_by_category, rng)
    stock, reorder_info = generate_stock_levels(items, demand_by_item, rng) 
    purchase_requests = generate_purchase_requests(items, stock, suppliers, reorder_info, rng)

    validate(items, demand_by_item, stock, reorder_info, purchase_requests)

    #formatting demand to match items
    final_demand = []
    for item_id, quantities in demand_by_item.items():
        for week, quantity in enumerate(quantities):
            row = {
                "item_id": item_id,
                "week_start": WEEK_START + timedelta(weeks=week),
                "quantity": int(quantity)
            }
            final_demand.append(row)

    files_to_generate = {
        "suppliers": suppliers,
        "items": items,
        "stock_levels": stock,
        "demand_history": final_demand,
        "purchase_requests": purchase_requests
    }

    for file_name, data in files_to_generate.items():
        generate_formatted_csv(file_name, data, COLUMN_NAMES[file_name])

    
if __name__ == "__main__":
    main()
    