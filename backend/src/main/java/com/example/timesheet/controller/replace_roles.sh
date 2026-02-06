#!/bin/bash

# Strategy: Replace old roles with placeholders first, then replace placeholders with new roles
# This avoids double-replacement issues

# Function to replace roles in a file
replace_roles_in_file() {
    local file=$1
    
    # Step 1: Replace DEPT_HEAD with [DEPT_HEAD_TEMP]
    sed -i '' 's/DEPT_HEAD/[DEPT_HEAD_TEMP]/g' "$file"
    
    # Step 2: Replace MANAGER with [MANAGER_TEMP]
    sed -i '' 's/MANAGER/[MANAGER_TEMP]/g' "$file"
    
    # Step 3: Replace EXECUTIVE with [EXECUTIVE_TEMP]
    sed -i '' 's/EXECUTIVE/[EXECUTIVE_TEMP]/g' "$file"
    
    # Step 4: Replace placeholders with new values
    # [DEPT_HEAD_TEMP] → MANAGER
    sed -i '' 's/\[DEPT_HEAD_TEMP\]/MANAGER/g' "$file"
    
    # [MANAGER_TEMP] → EXECUTIVE
    sed -i '' 's/\[MANAGER_TEMP\]/EXECUTIVE/g' "$file"
    
    # [EXECUTIVE_TEMP] → EMPLOYEE
    sed -i '' 's/\[EXECUTIVE_TEMP\]/EMPLOYEE/g' "$file"
}

# Process all controller files
for file in *.java; do
    echo "Processing $file..."
    replace_roles_in_file "$file"
    echo "✓ $file done"
done

echo "All role replacements completed!"
