import numpy as np

#Assigns suppliers attributes
def draw_supplier_attributes(supplier_info, rng):
    #lead time factor
    low, high = supplier_info["lead_time_factor"]
    lead_time_factor = round(float(rng.uniform(low, high)), 2)

    #lead time cv
    low, high = supplier_info["lead_time_cv"]
    lead_time_cv = round(float(rng.uniform(low, high)), 2)

    #on time rate
    low, high = supplier_info["on_time_rate"]
    on_time_rate = round(float(rng.uniform(low, high)), 2)

    return {
        "lead_time_factor": lead_time_factor,
        "lead_time_cv": lead_time_cv,
        "on_time_rate": on_time_rate,
    }

#Returns suppliers grouped by category
def group_suppliers_by_category(suppliers):
    by_category = {}
    for s in suppliers:
        by_category.setdefault(s["category"], []).append(s)
    return by_category

#Returns generated suppliers grouped by category
def generate_suppliers(company_names, supplier_info, categories, rng):
    names = list(company_names)
    rng.shuffle(names)

    suppliers = []
    low, high = supplier_info["per_category"]
    for category_name in categories:
        for _ in range(int(rng.integers(low, high+1))):
            suppliers.append({
                "supplier_id": len(suppliers) + 1,
                "name": names[len(suppliers)],
                "category": category_name,
                **draw_supplier_attributes(rng)
            })

    suppliers_by_category = group_suppliers_by_category(suppliers)
    return suppliers_by_category