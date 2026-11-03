import numpy as np
from config import COMPANY_NAMES, SUPPLIERS, CATEGORIES

def draw_supplier_attributes(rng):
    #lead time factor
    low, high = SUPPLIERS["lead_time_factor"]
    lead_time_factor = round(float(rng.uniform(low, high)), 2)

    #lead time cv
    low, high = SUPPLIERS["lead_time_cv"]
    lead_time_cv = round(float(rng.uniform(low, high)), 2)

    #on time rate
    low, high = SUPPLIERS["on_time_rate"]
    on_time_rate = round(float(rng.uniform(low, high)), 2)

    return {
        "lead_time_factor": lead_time_factor,
        "lead_time_cv": lead_time_cv,
        "on_time_rate": on_time_rate,
    }

def generate_suppliers(company_names, rng):
    names = list(company_names)
    rng.shuffle(names)

    suppliers = []
    low, high = SUPPLIERS["per_category"]
    for category_name in CATEGORIES:
        for _ in range(int(rng.integers(low, high+1))):
            suppliers.append({
                "supplier_id": len(suppliers) + 1,
                "name": names[len(suppliers)],
                "category": category_name,
                **draw_supplier_attributes(rng)
            })
    return suppliers