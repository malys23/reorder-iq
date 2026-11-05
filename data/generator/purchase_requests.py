from config import USER_IDS, CREATED_AT_RULES, AS_OF, STEP_GAPS, DSA_PROBABILITIES, HE_SHARES
from datetime import timedelta

def get_step_gaps(step, rng):
    return int(rng.choice(STEP_GAPS[step]["days"], p=STEP_GAPS[step]["probs"]))

def get_random_days(status, rng):
    low, high = CREATED_AT_RULES[status]
    return int(rng.integers(low, high+1))

def get_ordered_lead_time_fraction(status, rng):
    low, high = CREATED_AT_RULES[status]
    return round(float(rng.uniform(low, high)), 2)

def get_audit_trail_dates(item, supplier, status, rng):
    audit_trail_dates = {
            "created_at": None, 
            "submitted_at":None,
            "approved_at": None,
            "ordered_at": None,
            "received_at": None,
    }
    if status == "received":
        ontime_early_late = rng.choice([-1*max(1, round(item["lead_time_days"]*supplier["lead_time_cv"])), 0, max(1,round((item["lead_time_days"]*supplier["lead_time_cv"])))], p=(supplier["on_time_rate"] * 0.3, supplier["on_time_rate"] * 0.7, 1-supplier["on_time_rate"]))
        audit_trail_dates["received_at"] =  AS_OF - timedelta(days=get_random_days(status, rng))
        audit_trail_dates["ordered_at"] = audit_trail_dates["received_at"] - timedelta(days= int(max(1, item["lead_time_days"]+ontime_early_late)))
        audit_trail_dates["approved_at"] = audit_trail_dates["ordered_at"] - timedelta(days=get_step_gaps("approved_to_ordered", rng))
        audit_trail_dates["submitted_at"] = audit_trail_dates["approved_at"] - timedelta(days=get_step_gaps("submitted_to_approved", rng))
        audit_trail_dates["created_at"] = audit_trail_dates["submitted_at"] - timedelta(days=get_step_gaps("created_to_submitted", rng))
    elif status == "ordered":
        audit_trail_dates["ordered_at"] = AS_OF - timedelta(days= round((get_ordered_lead_time_fraction(status, rng))*item["lead_time_days"]))
        audit_trail_dates["approved_at"] = audit_trail_dates["ordered_at"] - timedelta(days=get_step_gaps("approved_to_ordered", rng))
        audit_trail_dates["submitted_at"] = audit_trail_dates["approved_at"] - timedelta(days=get_step_gaps("submitted_to_approved", rng))
        audit_trail_dates["created_at"] = audit_trail_dates["submitted_at"] - timedelta(days=get_step_gaps("created_to_submitted", rng))
    elif status == "approved":
        audit_trail_dates["approved_at"] =  AS_OF - timedelta(days=get_random_days(status, rng))
        audit_trail_dates["submitted_at"] = audit_trail_dates["approved_at"] - timedelta(days=get_step_gaps("submitted_to_approved", rng))
        audit_trail_dates["created_at"] = audit_trail_dates["submitted_at"] - timedelta(days=get_step_gaps("created_to_submitted", rng))
    elif status == "submitted":
        audit_trail_dates["submitted_at"] =  AS_OF - timedelta(days=get_random_days(status, rng))
        audit_trail_dates["created_at"] = audit_trail_dates["submitted_at"] - timedelta(days=get_step_gaps("created_to_submitted", rng))
    elif status == "draft":
        audit_trail_dates["created_at"] =  AS_OF - timedelta(days=get_random_days(status, rng))
    
    return audit_trail_dates

def draw_pr_attributes(item, item_stock, item_supplier, item_reorder_info, rng):
    audit_trail_users = {
        "requested_by": USER_IDS["planner"],
        "approved_by": None,
        "received_by": None,
    }
    if item_stock["on_order"]:
        quantity = item_stock["on_order"]
        status = "ordered"
        audit_trail_users["approved_by"] = USER_IDS["approver"]       
        audit_trail = audit_trail_users | get_audit_trail_dates(item, item_supplier, status, rng)  

    elif not item_stock["on_order"] and item_reorder_info["situation"] in ("below_rop", "stockout"):
        quantity = item_reorder_info["rop"] - item_stock["on_hand"]
        status = str(rng.choice(DSA_PROBABILITIES["step_name"], p=DSA_PROBABILITIES["probs"]))
        if status == "approved":
            audit_trail_users["approved_by"] = USER_IDS["approver"]  
            audit_trail = audit_trail_users | get_audit_trail_dates(item, item_supplier, status, rng)  
        else:
            audit_trail = audit_trail_users | get_audit_trail_dates(item, item_supplier, status, rng) 

    elif item_reorder_info["situation"] in ("healthy", "excess"):        
        quantity = 2 + rng.choice([item_reorder_info["rop"], round(0.75*item_reorder_info["rop"]), round(0.5*item_reorder_info["rop"]), round(0.25* item_reorder_info["rop"])], p = (0.25, 0.25, 0.25, 0.25))
        status = "received"
        audit_trail_users["received_by"] = USER_IDS["receiver"]
        audit_trail_users["approved_by"] = USER_IDS["approver"]
        audit_trail = audit_trail_users | get_audit_trail_dates(item, item_supplier, status, rng)

    needed_by= timedelta(days = item["lead_time_days"])+audit_trail["created_at"] + timedelta(days=2)

    return {
        "item_id": item["item_id"],
        "category_name": item["category"],
        "quantity": quantity,
        "needed_by": needed_by,
        "status": status,
        **audit_trail
    }


def make_pr(pr_id, item, item_stock, item_supplier, item_reorder_info, rng):
    attrs = draw_pr_attributes(item, item_stock, item_supplier, item_reorder_info, rng)
    purchase_request = {"request_id": pr_id, **attrs}
    return purchase_request
    
def generate_purchase_requests(items, stock, suppliers, reorder_info, rng):
    purchase_requests = []
    pr_count = 0
    for item in items:
        situation = reorder_info[item["item_id"]]["situation"]
        pr_prob = rng.random()
        if stock[item["item_id"]-1]["on_order"] or pr_prob < HE_SHARES[situation]:
            pr_count +=1
            purchase_request = make_pr(pr_count, item, stock[item["item_id"]-1], suppliers[item["supplier_id"]-1], reorder_info[item["item_id"]], rng )
            purchase_requests.append(purchase_request)
    return purchase_requests
