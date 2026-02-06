#!/bin/bash

echo "╔════════════════════════════════════════════════════════════════════════════╗"
echo "║                    ROLE MAPPING VERIFICATION SUMMARY                       ║"
echo "╚════════════════════════════════════════════════════════════════════════════╝"
echo ""

echo "ROLE MAPPING: OLD → NEW"
echo "  • MANAGER → EXECUTIVE (管理層, highest authority)"
echo "  • DEPT_HEAD → MANAGER (部門主管, department head)"  
echo "  • EXECUTIVE → EMPLOYEE (執行人員, worker)"
echo "  • PM → PM (no change)"
echo "  • HR → HR (no change)"
echo ""

files=("DepartmentController.java" "ProjectController.java" "ReportController.java" "TaskController.java" "TimeRequestController.java" "TimesheetController.java" "UserController.java")

for file in "${files[@]}"; do
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "File: $file"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    
    # Check for old role names (should not exist)
    old_count=$(grep -c "hasRole('MANAGER')\|hasRole('DEPT_HEAD')\|hasRole('EXECUTIVE')" "$file" 2>/dev/null || echo "0")
    
    # Count new role occurrences
    executive_count=$(grep -c "'EXECUTIVE'" "$file" 2>/dev/null || echo "0")
    manager_count=$(grep -c "'MANAGER'" "$file" 2>/dev/null || echo "0")
    employee_count=$(grep -c "'EMPLOYEE'" "$file" 2>/dev/null || echo "0")
    pm_count=$(grep -c "'PM'" "$file" 2>/dev/null || echo "0")
    hr_count=$(grep -c "'HR'" "$file" 2>/dev/null || echo "0")
    
    echo "  New role counts:"
    [ $executive_count -gt 0 ] && echo "    ✓ EXECUTIVE: $executive_count occurrences"
    [ $manager_count -gt 0 ] && echo "    ✓ MANAGER: $manager_count occurrences"
    [ $employee_count -gt 0 ] && echo "    ✓ EMPLOYEE: $employee_count occurrences"
    [ $pm_count -gt 0 ] && echo "    ✓ PM: $pm_count occurrences"
    [ $hr_count -gt 0 ] && echo "    ✓ HR: $hr_count occurrences"
    
    # Show a sample of PreAuthorize annotations
    echo ""
    echo "  Sample @PreAuthorize annotations:"
    grep "@PreAuthorize" "$file" | head -3 | sed 's/^/    /'
    echo ""
done

echo "╔════════════════════════════════════════════════════════════════════════════╗"
echo "║                        VERIFICATION COMPLETE                               ║"
echo "╚════════════════════════════════════════════════════════════════════════════╝"
