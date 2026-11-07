from collections import Counter, defaultdict
from utils import _report

#categories: the CATEGORIES dict from config (only its keys are used).
def check_suppliers(suppliers, categories, show=5):
    failures = defaultdict(list)
 
    ids = Counter(s["supplier_id"] for s in suppliers)
    failures["duplicate supplier_id"] = [i for i, n in ids.items() if n > 1]
    names = Counter(s["name"] for s in suppliers)
    failures["duplicate supplier name"] = [n for n, c in names.items() if c > 1]
 
    served = {s["category"] for s in suppliers}
    failures["category with no supplier"] = [c for c in categories if c not in served]
 
    for s in suppliers:
        sid = s["supplier_id"]
        if s["category"] not in categories:
            failures["supplier with unknown category"].append(sid)
        if not 0 < s["on_time_rate"] <= 1:
            failures["on_time_rate outside (0, 1]"].append(sid)
        if s["lead_time_cv"] < 0:
            failures["negative lead_time_cv"].append(sid)
 
    failures = {k: v for k, v in failures.items() if v}
    return _report("Supplier checks", f"{len(suppliers)} suppliers", failures, show)