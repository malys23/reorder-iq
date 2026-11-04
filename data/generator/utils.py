#Returns list of items, count of which predetermined with share
def build_counted_list(target_items, num_items, rng):
    counts = {}
    for name, cfg in target_items.items():
        counts[name] = round(num_items * cfg["share"])

    diff = num_items - sum(counts.values())
    largest = max(counts, key=counts.get)
    counts[largest] += diff

    names = []
    for name, n in counts.items():
        for _ in range(n):
            names.append(name)

    rng.shuffle(names)
    return names