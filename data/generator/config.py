#seed, number of items, weeks of history, parameter ranges

FIXED_SEED = 42
NUM_ITEMS = 300
WEEKS = 104
ABC_CUTOFFS = (0.80, 0.95)
CATEGORIES = {
    "fasteners": {
        "share": 0.20, "uom": "each",
        "unit_cost": (0.05, 3.00), "lead_time_days": (3, 10),
        "weekly_demand": (50, 500), "demand_prob": 1.0,
        "pattern": "smooth", "trend_pct_per_year": (-3, 5),
    },
    "consumables": {
        "share": 0.25, "uom": "each",
        "unit_cost": (3, 70), "lead_time_days": (2, 12),
        "weekly_demand": (20, 120), "demand_prob": 1.0,
        "pattern": "smooth", "trend_pct_per_year": (-3, 5),
    },
    "gaskets_seals": {
        "share": 0.07, "uom": "each",
        "unit_cost": (1.5, 300), "lead_time_days": (3, 8),
        "weekly_demand": (5, 150), "demand_prob": 1.0,
        "pattern": "seasonal", "trend_pct_per_year": (-3, 5),
        "season_strength": (0.25, 0.50), "peak_week": 12,
    },
    "fittings_flanges": {
        "share": 0.07, "uom": "each",
        "unit_cost": (3, 400), "lead_time_days": (2, 14),
        "weekly_demand": (5, 80), "demand_prob": 1.0,
        "pattern": "seasonal", "trend_pct_per_year": (-3, 5),
        "season_strength": (0.15, 0.35), "peak_week": 24,
    },
    "pipe": {
        "share": 0.11, "uom": "joint",
        "unit_cost": (100, 1500), "lead_time_days": (14, 32),
        "weekly_demand": (2, 40), "demand_prob": 1.0,
        "pattern": "seasonal", "trend_pct_per_year": (-3, 5),
        "season_strength": (0.15, 0.30), "peak_week": 32,
    },
    "valves": {
        "share": 0.10, "uom": "each",
        "unit_cost": (50, 2500), "lead_time_days": (3, 30),
        "weekly_demand": (2, 12), "demand_prob": (0.30, 0.60),
        "pattern": "intermittent", "trend_pct_per_year": (-3, 5),
    },
    "instrumentation": {
        "share": 0.10, "uom": "each",
        "unit_cost": (5, 1500), "lead_time_days": (10, 45),
        "weekly_demand": (2, 10), "demand_prob": (0.25, 0.50),
        "pattern": "intermittent", "trend_pct_per_year": (-3, 5),
    },
    "inserts": {
        "share": 0.10, "uom": "each",
        "unit_cost": (600, 2000), "lead_time_days": (14, 32),
        "weekly_demand": (5, 100), "demand_prob": (0.10, 0.25),
        "pattern": "lumpy", "trend_pct_per_year": (-3, 5),
    },
}

COMPANY_NAMES = [
    "White Mountain",
    "Gronger",
    "Firstenal",
    "Low Industries",
    "Moar Solutions",
    "RidgeCo",
    "Aceways",
    "Blossom Motors",
    "BridgeRock",
    "Pipes R Us",
    "WaterlooInc",
    "Reedsworth",
    "Junglectics",
    "Titaniumotors",
    "Maze Navigations",
    "Cyclolutions",
    "Sunlight Technologies",
    "Woodrover",
    "Cannonics",
    "Bearing Co",
    "Microlutions",
    "Boarwell",
    "Graniteworld",
    "Alphacast"
]
SUPPLIER_INFO = {
    "lead_time_factor": (0.8, 1.4),
    "lead_time_cv": (0.05, 0.30),
    "on_time_rate": (0.80, 0.99),
    "min_order_value": (0.5, 600.00),
    "per_category": (1, 3)
}

STATS_WEEKS = 26
Z_BY_CLASS= {
    "A": 2.05,  # ~98% service level
    "B": 1.645, # ~95%
    "C": 1.28   # 90%
}
STOCK_SITUATIONS = {
    "stockout": {"share": 0.05, "kind":"zero", "range": 0},
    "below_rop": {"share": 0.20,"kind": "rop_multiple", "range": (0.2, 1.0)},
    "healthy": {"share": 0.60, "kind":"days_above_top", "range": (0, 45)},
    "excess": {"share": 0.15, "kind": "days_above_top", "range": (100, 200)}
}