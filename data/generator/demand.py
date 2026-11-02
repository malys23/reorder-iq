#demand patterns: trend, seasonality, intermittent
import numpy as np

#Multiplier per week, growing or shrinking smoothly
def trend(weeks, pct_per_year):
    t=np.arange(weeks) / 52
    return (1 + pct_per_year/100) ** t

#Multiplier per week: 1 +/- strength, with a yearly cycle peakng at peak_week
def seasonality(weeks, strength, peak_week):
    t = np.arange(weeks)
    return 1 + strength * np.cos(2* np.pi * (t - peak_week) / 52)

def smooth_demand(item, weeks, rng):
    base = item["base_demand"]
    noise = rng.normal(1, 0.10, weeks)
    return base * trend(weeks, item["trend"]) * noise

def seasonal_demand(item, weeks, rng):
    base = smooth_demand(item, weeks, rng)
    return base * trend(weeks, item["season_strength"], item["peak_week"])

def intermittent_demand(item, weeks, rng):
    occurs = rng.random(weeks) < item["demand_prob"]
    size = rng.poisson(item["base_demand"], weeks)
    return occurs * size * trend(weeks, item["trend"])

def lumpy_demand(item, weeks, rng):
    occurs = rng.random(weeks) < item["demand_prob"]
    size = rng.lognormal(np.log(item["base_demand"]), 0.8, weeks)
    return occurs * size * trend(weeks, item["trend"])

PATTERNS ={
    "smooth": smooth_demand,
    "seasonal": seasonal_demand,
    "intermittent": intermittent_demand,
    "lumpy": lumpy_demand,
}

def generate_demand(item, weeks, rng):
    raw = PATTERNS[item["pattern"]](item, weeks, rng)
    return np.maximum(np.rint(raw), 0).astype(int)