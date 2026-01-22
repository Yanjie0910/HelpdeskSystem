# 📊 Helpdesk Reporting Module

The **Helpdesk Reporting Module** provides comprehensive reporting and analytics for the **University Malaya Helpdesk System**. It enables administrators to generate data exports, analyze SLA failure rates, and forecast future ticket volumes using statistical methods.

---

## ✨ Features Overview

### 1. Manage Saved Reports (CRUD)

* Save generated reports to the database for archival purposes.
* View previously generated reports.
* Track report metadata (type, format, timestamp, file path).
* Delete old or unused reports.

### 2. Generate Custom Data Export

* Export ticket data in:

    * **CSV** format (Excel / data analysis)
    * **Text (TXT)** format (summary-based reporting)
* Supports filtered exports based on ticket criteria (e.g., volume, performance).

### 3. Analyze Failure Rates (SLA Breaches)

* Calculates the percentage of tickets that missed their SLA deadlines.
* SLA rules are defined by ticket priority:

| Priority | SLA Duration |
| -------- | ------------ |
| Urgent   | 24 hours     |
| High     | 48 hours     |
| Medium   | 72 hours     |
| Low      | 7 days       |

* Provides a **risk classification** (e.g., `STABLE`, `WARNING`, `CRITICAL`).

### 4. Forecast Ticket Trends

* Uses **Linear Regression** on historical ticket data.
* Analyzes ticket volumes from the **last 3 weeks**.
* Predicts ticket volume for the **upcoming week**.

---

## ▶️ Running the Reporting Demo

The module includes a **console-based interactive demo** to test all features without a frontend UI.

### Step 1: Start the Application

Run the main Spring Boot application:

```
src/main/java/com/um/helpdesk/HelpdeskSpringbootApplication.java
```

---

### Step 2: Log In as Administrator

The Reporting Module is restricted to **Admin users only**.

1. When the **"SELECT DEMO USER"** menu appears, enter:

   ```
   1
   ```
2. This logs you in as:

    * **User**: Dr. World
    * **Role**: ADMIN

---

### Step 3: Access the Reporting Module

From the **ADMIN MENU**, select:

```
2. Reporting Module (View Stats / Generate Reports)
```

---

### Step 4: Use Available Features

You will see the **REPORTING COMPONENT TEST MENU**:

* **Option 1 – Generate Report**
  Example input:

    * Report Type: `OpenTickets`
    * Format: `CSV`

  The generated file will be saved to your **Desktop**.

* **Option 3 – Analyze Failure Rates**
  Displays real-time SLA breach statistics based on current ticket data.

* **Option 4 – Forecast Ticket Trends**
  Shows regression analysis and predicted ticket volume for next week.

---

## 🧪 Test Data Notes

* The `DataInitializer` automatically injects:

    * Tickets from **1–3 weeks ago**
    * Multiple **overdue tickets**

This ensures:

* SLA breach calculations are immediately visible
* Trend forecasting produces meaningful results

No manual data setup is required for testing.

---

## 🧑‍💻 Developer API Usage

### Service Interface

```
com.um.helpdesk.service.ReportingService
```

---

### 1. Dependency Injection

Inject the service into your Controller, Service, or Runner class:

```java
@Autowired
private ReportingService reportingService;
```

---

### 2. Generate a Report

Generates a physical file (CSV or TXT) and stores report metadata in the database.

```java
SavedReportArchive report = reportingService.generateReport("Monthly_Report", "CSV");

System.out.println("File saved at: " + report.getFilePath());
```

---

### 3. Get SLA Failure Rate Analysis

Returns statistical insights on SLA breaches.

```java
Map<String, Object> stats = reportingService.getFailureRateAnalysis();

System.out.println("Failure Rate: " + stats.get("Failure Rate"));
System.out.println("Risk Level: " + stats.get("Risk Level")); // e.g., STABLE, CRITICAL
```

---

### 4. Forecast Ticket Trends

Returns weekly ticket counts and the predicted ticket volume for the next week.

```java
Map<String, Integer> forecast = reportingService.getTicketTrendForecast();

int nextWeekPrediction = forecast.get("Next Week (Forecast)");
```

---

### 5. Manage Saved Reports

Standard CRUD operations for archived report metadata.

```java
// Retrieve all archived reports
List<SavedReportArchive> archives = reportingService.getAllSavedReports();

// Delete a report record (and optionally its file)
reportingService.deleteReport(reportId);
```

---

## 📌 Summary

The Helpdesk Reporting Module enhances administrative visibility by providing:

* Actionable SLA insights
* Predictive analytics for workload planning
* Flexible report generation and archival

It is fully testable via a console-based demo and integrates cleanly through a well-defined service API.
