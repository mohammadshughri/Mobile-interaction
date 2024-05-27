import pandas as pd

# Read the text file
txt_file_path = "log1714575507336.txt"  # Replace with the actual path to your txt file
data = []

# Open the text file and read lines
with open(txt_file_path, 'r') as file:
    # Assuming each line is a log entry
    for line in file:
        line = line.strip()  # Remove leading/trailing whitespace/newlines
        if line.startswith("END"):  # Only process lines that start with 'END'
            data.append(line.split(";"))

# Create a DataFrame from the log data
df = pd.DataFrame(data, columns=["Type", "Block", "Sentence", "KeyPresses", "Input", "EditDistance", "Timestamp"])

# Convert data types as needed
df["Block"] = df["Block"].astype(int)
df["KeyPresses"] = df["KeyPresses"].astype(int)
df["EditDistance"] = df["EditDistance"].astype(int)
df["Timestamp"] = df["Timestamp"].astype(int)

# Save the DataFrame to a CSV file
csv_file_path = "output.csv"  # Define your CSV file path
df.to_csv(csv_file_path, index=False)  # Save without row indices

print(f"Data has been exported to {csv_file_path}")
