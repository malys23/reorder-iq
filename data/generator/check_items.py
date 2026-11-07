from collections import Counter, defaultdict
from utils import _report
ABC_CLASSES = ("A", "B", "C")

#categories: the CATEGORIES dict from config (keys and 'share' are used).
def check_items(items, suppliers, categories, num_items, show=5):
    failures = defaultdict(list)
    suppliers_by_id = {s["supplier_id"]: s for s in suppliers}
 
    # count and ids
    if len(items) != num_items:
        failures["number of items != NUM_ITEMS"].append(len(items))
    ids = Counter(i["item_id"] for i in items)
    failures["duplicate item_id"] = [i for i, n in ids.items() if n > 1]
    if sorted(ids) != list(range(1, len(items) + 1)):
        failures["item_ids are not 1..N"].append("see ids")
 
    # category counts match the shares (within one item, because of rounding)
    counts = Counter(i["category"] for i in items)
    for name, cfg in categories.items():
        expected = cfg["share"] * num_items
        if abs(counts.get(name, 0) - expected) > 1:
            failures["category count far from its share"].append(
                f"{name}: {counts.get(name, 0)} vs {expected:.1f}")
 
    names = Counter(i["name"] for i in items) if items and "name" in items[0] else {}
    failures["duplicate item name"] = [n for n, c in names.items() if c > 1]
 
    for item in items:
        iid = item["item_id"]
        sup = suppliers_by_id.get(item["supplier_id"])
        if sup is None:
            failures["item with unknown supplier"].append(iid)
        elif sup["category"] != item["category"]:
            failures["supplier category != item category"].append(iid)
        if item["abc_class"] not in ABC_CLASSES:
            failures["item without a valid abc_class"].append(iid)
        if not item["unit_cost"] > 0:
            failures["unit_cost not positive"].append(iid)
        if not (item["lead_time_days"] == int(item["lead_time_days"]) and item["lead_time_days"] >= 1):
            failures["lead_time_days not a whole number >= 1"].append(iid)
 
    classes = Counter(i["abc_class"] for i in items)
    for c in ABC_CLASSES:
        if classes.get(c, 0) == 0:
            failures["abc class with no items"].append(c)
 
    failures = {k: v for k, v in failures.items() if v}
    summary = f"{len(items)} items, classes {dict(sorted(classes.items()))}"
    return _report("Item checks", summary, failures, show)