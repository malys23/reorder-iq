from collections import defaultdict

STOCK_FIELDS = {"item_id", "on_hand", "on_order"}

#Checks the stock table against the reorder info (situation and ROP).
# Prints a summary and returns {check name: list of offending item_ids}.
def check_stock(items, stock, reorder_info, show=5):
    failures = defaultdict(list)

    # 1. one stock row per item, same ids
    if len(stock) != len(items):
        failures["len(stock) != len(items)"].append(len(stock))
    if {row["item_id"] for row in stock} != {item["item_id"] for item in items}:
        failures["stock and items have different item_ids"].append("see ids")

    for row in stock:
        item_id = row["item_id"]
        on_hand, on_order = row["on_hand"], row["on_order"]
        info = reorder_info[item_id]
        situation, rop = info["situation"], info["rop"]

        # 2. clean rows: only the columns that go into stock_levels
        if set(row) != STOCK_FIELDS:
            failures["stock row has unexpected fields"].append(item_id)

        # 3. non-negative whole numbers
        if on_hand < 0 or on_order < 0:
            failures["negative on_hand or on_order"].append(item_id)
        if on_hand != int(on_hand) or on_order != int(on_order):
            failures["on_hand or on_order not whole"].append(item_id)

        # 4. each situation has the stock it should have
        if situation == "stockout" and on_hand != 0:
            failures["stockout with on_hand != 0"].append(item_id)
        if situation == "below_rop" and not on_hand < rop:
            failures["below_rop with on_hand >= rop"].append(item_id)
        if situation in ("healthy", "excess") and on_hand < rop:
            failures["healthy or excess with on_hand < rop"].append(item_id)
        if situation == "excess" and on_order != 0:
            failures["excess with an open order"].append(item_id)

        # 5. below-ROP and stockout items with an open order are covered by it
        if situation in ("below_rop", "stockout") and on_order > 0:
            if on_hand + on_order < rop:
                failures["open order does not bring position up to rop"].append(item_id)

    # summary
    counts = defaultdict(int)
    for info in reorder_info.values():
        counts[info["situation"]] += 1
    print(f"Stock checks: {len(stock)} rows, situations {dict(counts)}")
    if not failures:
        print("  all checks passed")
    for name, ids in failures.items():
        print(f"  FAILED: {name}: {len(ids)}  e.g. {ids[:show]}")
    return dict(failures)