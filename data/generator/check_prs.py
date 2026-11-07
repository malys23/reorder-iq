from collections import Counter, defaultdict
from datetime import timedelta

DATE_FIELDS = ["created_at", "submitted_at", "approved_at", "ordered_at", "received_at"]
# How many of the date fields (in order) must be filled for each status
DATES_FILLED = {"draft": 1, "submitted": 2, "approved": 3, "ordered": 4, "received": 5}

#Checks purchase requests against items and stock. Prints a summary 
# and returns a dict {check name: list of offending request_ids / item_ids}.
def check_purchase_requests(items, stock, purchase_requests, as_of, show=5):

    items_by_id = {item["item_id"]: item for item in items}
    stock_by_id = {row["item_id"]: row for row in stock}
    failures = defaultdict(list)

    # 1. request ids are unique
    id_counts = Counter(pr["request_id"] for pr in purchase_requests)
    for request_id, n in id_counts.items():
        if n > 1:
            failures["duplicate request_id"].append(request_id)

    # 2. open orders and ordered requests agree, one to one
    ordered = [pr for pr in purchase_requests if pr["status"] == "ordered"]
    ordered_by_item = defaultdict(list)
    for pr in ordered:
        ordered_by_item[pr["item_id"]].append(pr)

    for item_id, prs in ordered_by_item.items():
        if len(prs) > 1:
            failures["item has more than one ordered request"].append(item_id)
        if stock_by_id[item_id]["on_order"] == 0:
            failures["ordered request for item with no on_order"].append(item_id)

    for item_id, row in stock_by_id.items():
        if row["on_order"] > 0:
            prs = ordered_by_item.get(item_id, [])
            if not prs:
                failures["on_order item without ordered request"].append(item_id)
            elif prs[0]["quantity"] != row["on_order"]:
                failures["ordered quantity != on_order"].append(item_id)

    for pr in purchase_requests:
        rid = pr["request_id"]
        status = pr["status"]

        # 3. dates: right ones filled, in order, none after as_of
        n_filled = DATES_FILLED[status]
        values = [pr[f] for f in DATE_FIELDS]
        filled, empty = values[:n_filled], values[n_filled:]
        if any(v is None for v in filled):
            failures["missing date for status"].append(rid)
        elif any(v is not None for v in empty):
            failures["date filled beyond status"].append(rid)
        else:
            if any(a > b for a, b in zip(filled, filled[1:])):
                failures["dates out of order"].append(rid)
            if any(v > as_of for v in filled):
                failures["date after as_of"].append(rid)
            if pr["needed_by"] is not None and pr["needed_by"] <= filled[0]:
                failures["needed_by not after created_at"].append(rid)

        # 4. ordered requests are still in transit
        if status == "ordered" and pr["ordered_at"] is not None:
            lead = items_by_id[pr["item_id"]]["lead_time_days"]
            if pr["ordered_at"] + timedelta(days=int(lead)) < as_of:
                failures["ordered request should already have arrived"].append(rid)

        # 5. users
        if pr["requested_by"] is None:
            failures["requested_by missing"].append(rid)
        needs_approver = status in ("approved", "ordered", "received")
        if needs_approver != (pr["approved_by"] is not None):
            failures["approved_by wrong for status"].append(rid)
        if (status == "received") != (pr["received_by"] is not None):
            failures["received_by wrong for status"].append(rid)

        # 6. quantity is a positive whole number
        q = pr["quantity"]
        if not (q == int(q) and q > 0):
            print(f'negative quantity at {rid}: ', q) 
            failures["quantity not a positive integer"].append(rid)

    # summary
    print(f"Purchase request checks: {len(purchase_requests)} requests, "
          f"{len(ordered)} ordered, {sum(1 for r in stock if r['on_order'] > 0)} items with on_order")
    if not failures:
        print("  all checks passed")
    for name, ids in failures.items():
        print(f"  FAILED: {name}: {len(ids)}  e.g. {ids[:show]}")
    return dict(failures)