import numpy as np
from collections import Counter
from config import FIXED_SEED, COMPANY_NAMES, SUPPLIER_INFO, CATEGORIES
from suppliers import generate_suppliers
from items import generate_items


def main():
    rng = np.random.default_rng(FIXED_SEED)
    suppliers, suppliers_by_category = generate_suppliers(COMPANY_NAMES, SUPPLIER_INFO, CATEGORIES, rng)
    items, demand_by_item = generate_items(suppliers_by_category, rng)

    # Sanity check on generation
    print(len(items), Counter((i["category"], i["abc_class"]) for i in items))

    #Demand checks
    


if __name__ == "__main__":
    main()
    