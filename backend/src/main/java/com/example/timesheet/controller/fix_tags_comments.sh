#!/bin/bash

# Update ProjectController
sed -i '' 's/"專案管理 API (Manager)"/"專案管理 API (Executive)"/g' ProjectController.java
sed -i '' 's/"Manager\/PM"/"Executive\/PM"/g' TimeRequestController.java
sed -i '' 's/"部門管理 API (HR、部門主管)"/"部門管理 API (HR、部門經理)"/g' DepartmentController.java
sed -i '' 's/"工時報表 API (Department Head \/ Manager \/ PM)"/"工時報表 API (Manager \/ Executive \/ PM)"/g' ReportController.java
sed -i '' 's/"任務管理 API (PM\/Executive)"/"任務管理 API (PM\/Employee)"/g' TaskController.java
sed -i '' 's/"工時記錄 API (Executive)"/"工時記錄 API (Employee)"/g' TimesheetController.java

echo "Tag descriptions updated!"
