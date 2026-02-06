#!/bin/bash

# Fix ProjectController - "Only MANAGER" → "Only EXECUTIVE"
sed -i '' 's/Only MANAGER can create projects/Only EXECUTIVE can create projects/g' ProjectController.java
sed -i '' 's/Only MANAGER can update projects/Only EXECUTIVE can update projects/g' ProjectController.java
sed -i '' 's/Only MANAGER can close projects/Only EXECUTIVE can close projects/g' ProjectController.java
sed -i '' 's/Only MANAGER users have access/Only EXECUTIVE users have access/g' ProjectController.java

# Fix ReportController - mentions of "DEPT_HEAD, MANAGER, PM" 
sed -i '' 's/Only DEPT_HEAD, MANAGER, PM/Only MANAGER, EXECUTIVE, PM/g' ReportController.java

# Fix TaskController - "EXECUTIVE users" → "EMPLOYEE users"
sed -i '' 's/EXECUTIVE users can complete tasks/EMPLOYEE users can complete tasks/g' TaskController.java
sed -i '' 's/Only EXECUTIVE can complete tasks/Only EMPLOYEE can complete tasks/g' TaskController.java
sed -i '' 's/PM users have access to create\/update\/delete endpoints./PM users have access to create\/update\/delete endpoints./g' TaskController.java
sed -i '' 's/EXECUTIVE users can complete tasks/EMPLOYEE users can complete tasks/g' TaskController.java

# Fix TimeRequestController - MANAGER → EXECUTIVE
sed -i '' 's/Only MANAGER can view/Only EXECUTIVE can view/g' TimeRequestController.java
sed -i '' 's/Only MANAGER can approve/Only EXECUTIVE can approve/g' TimeRequestController.java
sed -i '' 's/MANAGER users approve/EXECUTIVE users approve/g' TimeRequestController.java

# Fix TimesheetController - EXECUTIVE → EMPLOYEE
sed -i '' 's/Only EXECUTIVE role/Only EMPLOYEE role/g' TimesheetController.java

# Fix DepartmentController - "department heads" → "department managers"
sed -i '' 's/Available to HR and department heads/Available to HR and department managers/g' DepartmentController.java

echo "Comments updated!"
