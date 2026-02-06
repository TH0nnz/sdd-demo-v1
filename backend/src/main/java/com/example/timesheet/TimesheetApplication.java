package com.example.timesheet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main application class for Timesheet Management System.
 * 
 * This application provides enterprise-grade timesheet management with:
 * - Role-based access control (5 roles)
 * - Project and task hierarchy
 * - Work hours calculation with lunch break deduction
 * - Real-time progress monitoring
 * - Comprehensive reporting
 */
@SpringBootApplication
@EnableJpaAuditing
public class TimesheetApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimesheetApplication.class, args);
    }

}
