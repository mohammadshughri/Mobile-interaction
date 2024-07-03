import numpy as np
from dtaidistance import dtw

# Define the sequences
template = np.array([9, 7, 6, 5, 4, 1, 8, 11, 6, 3, 3, 1, 2])
input_seq = np.array([7, 7, 2, 9, 1, 1])

# Compute DTW
alignment = dtw.warping_path(template, input_seq)

# Extract the DTW distance (minimal cost)
minimal_cost = dtw.distance(template, input_seq)

# Compute the DTW matrix
dtw_matrix = dtw.warping_paths(template, input_seq)

# Print results
print("DTW Matrix:")
print(dtw_matrix)
print("\nOptimal Path:")
print(alignment)
print("\nMinimal Cost:")
print(minimal_cost)

# Optionally, visualize the alignment
from dtaidistance import dtw_visualisation as dtwvis
import matplotlib.pyplot as plt

fig, ax = plt.subplots(nrows=2, ncols=1, figsize=(10, 10))
dtwvis.plot_warping(template, input_seq, alignment, ax=ax[0])
dtwvis.plot_warpingpaths(
    template, input_seq, dtw_matrix, alignmentB=alignment, ax=ax[1]
)
plt.tight_layout()
plt.show()
