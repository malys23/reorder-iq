import numpy as np
from collections import Counter
import matplotlib.pyplot as plt
from config import FIXED_SEED, COMPANY_NAMES, SUPPLIER_INFO, CATEGORIES
from suppliers import generate_suppliers
from items import generate_items


def main():
    rng = np.random.default_rng(FIXED_SEED)
    suppliers, suppliers_by_category = generate_suppliers(COMPANY_NAMES, SUPPLIER_INFO, CATEGORIES, rng)
    items, demand_by_item = generate_items(suppliers_by_category, rng)

    # Sanity check on generation
    print(len(items), Counter((i["category"], i["abc_class"]) for i in items))
    print(min(i["lead_time_days"] for i in items), max(i["lead_time_days"] for i in items))
    
    #Demand checks
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

if __name__ == "__main__":
    main()
    