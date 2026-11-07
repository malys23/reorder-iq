import numpy as np
import matplotlib.pyplot as plt
from check_stock import check_stock
from check_prs import check_purchase_requests
from config import AS_OF

#Keeping some validation code to later format for possible testing

def validate(items, demand_by_item, stock, reorder_info, purchase_requests):
    # # Sanity check on generation
    # print(len(items), Counter((i["category"], i["abc_class"]) for i in items))
    # print(min(i["lead_time_days"] for i in items), max(i["lead_time_days"] for i in items))
    
    # #Demand checks with plots
    # examples = {}
    # for item in items:
    #     examples.setdefault(item["pattern"], item)

    # fig, axes = plt.subplots(2, 2, figsize=(11, 7), sharex=True)
    # for ax, (pattern, item) in zip(axes.flat, examples.items()):
    #     ax.plot(demand_by_item[item["item_id"]])
    #     ax.set_title(f'{pattern}: {item["category"]} (item {item["item_id"]})')
    #     ax.set_xlabel("week")
    #     ax.set_ylabel("units")
    # plt.tight_layout()
    # plt.show()

    #Sanity check for stock
    check_stock(items, stock, reorder_info, show=5)

    #Sanity check for prs
    check_purchase_requests(items, stock, purchase_requests, AS_OF, show=5)