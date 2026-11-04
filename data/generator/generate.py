import numpy as np
from collections import Counter
import matplotlib.pyplot as plt
from config import FIXED_SEED, COMPANY_NAMES, SUPPLIER_INFO, CATEGORIES
from suppliers import generate_suppliers
from items import generate_items
from stock import generate_stock_levels
from validate import validate

def main():
    rng = np.random.default_rng(FIXED_SEED)
    suppliers, suppliers_by_category = generate_suppliers(COMPANY_NAMES, SUPPLIER_INFO, CATEGORIES, rng)
    items, demand_by_item = generate_items(suppliers_by_category, rng)
    stock = generate_stock_levels(items, demand_by_item, rng) 

if __name__ == "__main__":
    main()
    