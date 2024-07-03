import numpy as np


def dtw(template, input_seq):
    n, m = len(template), len(input_seq)
    dtw_table = np.zeros((m, n))

    # Fill the first row
    dtw_table[0, :] = np.cumsum([abs(input_seq[0] - t) for t in template])

    # Fill the first column
    dtw_table[:, 0] = np.cumsum([abs(i - template[0]) for i in input_seq])

    # Fill the rest of the table
    for i in range(1, m):
        for j in range(1, n):
            cost = abs(input_seq[i] - template[j])
            dtw_table[i, j] = cost + min(
                dtw_table[i - 1, j], dtw_table[i, j - 1], dtw_table[i - 1, j - 1]
            )

    # Find the optimal path
    path = [(m - 1, n - 1)]
    i, j = m - 1, n - 1
    while i > 0 or j > 0:
        if i == 0:
            j -= 1
        elif j == 0:
            i -= 1
        else:
            min_cost = min(
                dtw_table[i - 1, j], dtw_table[i, j - 1], dtw_table[i - 1, j - 1]
            )
            if min_cost == dtw_table[i - 1, j - 1]:
                i, j = i - 1, j - 1
            elif min_cost == dtw_table[i - 1, j]:
                i -= 1
            else:
                j -= 1
        path.append((i, j))

    path.reverse()

    return dtw_table, path, dtw_table[-1, -1]


# Define the sequences
template = [9, 7, 6, 5, 4, 1, 8, 11, 6, 3, 3, 1, 2]
input_seq = [7, 7, 2, 9, 1, 1]

# Compute DTW
dtw_table, optimal_path, minimal_cost = dtw(template, input_seq)

# Print results
print("DTW Table:")
print(dtw_table)
print("\nOptimal Path:")
print(optimal_path)
print("\nMinimal Cost:")
print(minimal_cost)
