import numpy as np
from collections import Counter
import matplotlib.pyplot as plt

#Keeping some validation code to later format for possible testing
#In order to run the tests, you will need to add "situation": situation, "rop": rop to line 57 of stock.py
#(add those two parameters to stock)

def validate(items, demand_by_item, stock):
    # Sanity check on generation
    print(len(items), Counter((i["category"], i["abc_class"]) for i in items))
    print(min(i["lead_time_days"] for i in items), max(i["lead_time_days"] for i in items))
    
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

    #Sanity check for stock
    print("len(stock) == len(items): ", len(stock) == len(items))
    print(len(stock), Counter((i["situation"]) for i in stock))
    for i in range(len(stock)):
        print(f"stock[{i}]: ", stock[i])

        #Check items that on hand but below rop and have order open will have enough stock to cover rop
        if(stock[i]["on_hand"] < stock[i]["rop"] and stock[i]["on_order"]):
            print("below rop but with order placed", stock[i]["on_hand"] + stock[i]["on_order"] >= stock[i]["rop"])

        #Check item is truly healthy or excess
        if(stock[i]["situation"] in ("healthy", "excess")):
            print("healthy: ", stock[i]["on_hand"] >= stock[i]["rop"])

        #Check if stockout items actually have 0 items on hand
        if(stock[i]["situation"] == "stockout"):
            print("stockout?: ", stock[i]["on_hand"] == 0)

        #Check if a situation is excess it has no orders open
        if(stock[i]["situation"] == "excess"):
            print("excess on_order is 0: ", stock[i]["on_order"] == 0)

        #Check all items are non negative
        if(stock[i]["on_hand"]>=0 and stock[i]["on_order"] >=0):
            print("on_hand and on_order non-negative: ", True)
        else: 
            print("on_hand and on_order non-negative: ", False)