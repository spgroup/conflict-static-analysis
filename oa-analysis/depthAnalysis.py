import json
import csv
import matplotlib.pyplot as plt

with open('out.json') as f:
    data = json.load(f)

l_lengths = []
r_lengths = []
diffs = []
rows = []
depth = []
same_depth_count = 0

for idx, entry in enumerate(data):
    interference = entry['body']['interference']
    l_stack = interference[0]['stackTrace']
    r_stack = interference[1]['stackTrace']
    
    l_len = len(l_stack)
    r_len = len(r_stack)
    diff = abs(l_len - r_len)
    max_len = max(l_len, r_len)

    depth.append(max_len)
    l_lengths.append(l_len)
    r_lengths.append(r_len)
    diffs.append(diff)
    
    if l_len == r_len:
        same_depth_count += 1
        
    l_class = interference[0]['location'].get('class', None)
    r_class = interference[1]['location'].get('class', None)
    l_method = interference[0]['location'].get('method', None)
    r_method = interference[1]['location'].get('method', None)
    same_class = l_class == r_class and l_class is not None
    same_method = same_class and l_method == r_method and l_method is not None
    rows.append([idx, l_len, r_len, max_len, diff, same_class, same_method])

with open('conflict_stacktrace_stats.csv', 'w', newline='') as csvfile:
    writer = csv.writer(csvfile)
    writer.writerow(['conflict_index', 'left_stacktrace_length', 'right_stacktrace_length', 'conflict_depth', 'stacktrace_diff', 'same_class', 'same_method'])
    writer.writerows(rows)


plt.figure(figsize=(10, 5))
plt.plot(l_lengths, label='L branch stackTrace length', marker='o')
plt.plot(r_lengths, label='R branch stackTrace length', marker='x')
plt.title('Depth per Conflict')
plt.xlabel('Conflict Index')
plt.ylabel('Depth')
plt.legend()
plt.xticks(range(0, len(l_lengths), max(1, len(l_lengths)//10))) 
plt.tight_layout()
plt.savefig('depths.png')
plt.close()

plt.figure(figsize=(6, 4))
plt.hist(diffs, bins=range(0, max(diffs)+2, 1), align='left', rwidth=0.8)
plt.title('Histogram of branches depth differences (|L-R|)')
plt.xlabel('Absolute Difference')
plt.ylabel('Frequency')
plt.xticks(range(0, max(diffs)+2, 1))
plt.tight_layout()
plt.savefig('diff_hist.png')
plt.close()

plt.figure(figsize=(8, 5))
plt.hist(depth, bins=range(min(depth), max(depth)+2, 1), align='left', rwidth=0.8)
plt.title('Depth Histogram')
plt.xlabel('Depth')
plt.ylabel('Frequency')
plt.xticks(range(min(depth), max(depth)+2, 1))
plt.tight_layout()
plt.savefig('depth_hist.png')
plt.close()

same_method_values = [row[6] for row in rows]
plt.figure(figsize=(5, 4))
plt.hist(same_method_values, bins=[-0.5, 0.5, 1.5], rwidth=0.6)
plt.title('Conflicts in Same Method')
plt.xlabel('Same Method (False=0, True=1)')
plt.ylabel('Frequency')
plt.xticks([0, 1], ['False', 'True'])
plt.tight_layout()
plt.savefig('same_method_hist.png')
plt.close()

print(f"Total conflicts analyzed: {len(data)}")
print(f"Conflicts with same branches depth: {same_depth_count}")
print(f"Maximum conflict depth: {max(depth)}")
print(f"Average conflict depth: {sum(depth)/len(depth):.2f}")
print(f"Average conflict branches difference: {sum(diffs)/len(diffs):.2f}")
