import numpy as np
from items import generate_items
from config import FIXED_SEED

def main():
    rng = np.random.default_rng(FIXED_SEED)
    items, demand_by_item = generate_items(rng)

    