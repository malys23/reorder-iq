import csv
from config import OUTPUT_DIR

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

#returns formatte csv file
def generate_formatted_csv(file_name, rows, column_names):
    file_path = OUTPUT_DIR / f"{file_name}.csv"
    with open(file_path, 'w', newline='', encoding="utf-8") as csvfile:
        writer = csv.DictWriter(csvfile, fieldnames=column_names, extrasaction="ignore")
        writer.writeheader()
        writer.writerows(rows)