# 📊 Reporting Component (OSGi Bundle)

## Overview

This component provides **Reporting & Analytics** capabilities for the Helpdesk System. It is designed as an **OSGi bundle** that handles report generation, data export simulation, ticket trend forecasting, and failure rate analysis.

> ⚠️ **IMPORTANT ARCHITECTURAL NOTE**
> To ensure stability within the OSGi container (**Apache Felix**) and minimize dependency conflicts for this assignment, this component operates in **In-Memory Mode**.
>
> * **No External Database:** Does not connect to H2 or MySQL
> * **Mock Data:** Uses internal static data to simulate tickets and saved reports
> * **Volatile Storage:** All data is lost when the bundle is stopped or the container is restarted

---

## ✅ Implemented Features

This component fulfills the following functional requirements:

### 1. Manage Saved Reports (CRUD)

* **Create:** Generate new reports based on ticket data
* **Read:** View a list of all archived reports
* **Update:** Modify report metadata (e.g., rename a report)
* **Delete:** Remove old reports from the archive

### 2. Generate Custom Data Export

* Simulates exporting data to local files on the user's **Desktop**
* Supports formats such as:

    * **CSV** – Structured data export
    * **Graph** – Text-based visual representation

### 3. Analyze System Failure Rates

* Analyzes mock ticket data to track overdue tickets
* Calculates failure rates based on missed deadlines

### 4. Forecast Ticket Trends

* Uses historical mock data to predict future ticket volume
* Helps identify potential spikes in service requests

---

## 🛠️ Installation & Deployment

### 1. Build the Bundle

Navigate to the `reporting-component` directory and run:

```bash
mvn clean install
```

### 2. Deploy to Apache Felix

Start your Apache Felix container:

```bash
java -jar bin/felix.jar
```

Then execute the following commands in the **g! console**:

```bash
# 1. Install the bundle (update the path to your actual location)
install file:///path/to/helpdesk-osgi/reporting-component/target/reporting-component-1.0.0.jar

# 2. Start the bundle
start <BUNDLE_ID>
```

> Replace `<BUNDLE_ID>` with the ID returned by the `install` command (e.g., `start 15`).

---

## 🔌 Service API Usage

Other components (e.g., `application-component` or `web-console`) can consume this functionality via the **ReportingService** interface.

**Interface:**

```
com.um.helpdesk.service.ReportingService
```

### Code Examples

#### 1. Generating a Report

```java
ReportingService service = ...; // Obtained via OSGi context

// Generates a CSV report and saves it to the Desktop
SavedReportArchive report = service.generateReport("TicketVolume", "CSV");
System.out.println("Report saved at: " + report.getFilePath());
```

#### 2. Failure Rate Analysis

```java
Map<String, Object> stats = service.getFailureRateAnalysis();

System.out.println("Total Overdue: " + stats.get("Missed Deadlines"));
System.out.println("Failure Rate: " + stats.get("Failure Rate"));
```

#### 3. Forecasting Ticket Trends

```java
Map<String, Integer> trends = service.getTicketTrendForecast();
System.out.println("Next Week Prediction: " + trends.get("Next Week (Predicted)"));
```

#### 4. Updating a Report

```java
// Rename an existing report
report.setReportName("Updated Monthly Analysis");
service.updateReport(report);
```

---

## 📂 Internal Mock Data

Since there is no database connection, the `ReportingServiceImpl` initializes with mock ticket data to support logic testing:

| Ticket ID | Status | Condition | Used For         |
| --------- | ------ | --------- | ---------------- |
| T1        | Closed | On Time   | Trend History    |
| T2        | Closed | Late      | Failure Analysis |
| T3        | Open   | Normal    | Current Volume   |
| T4        | Open   | Overdue   | Failure Analysis |
| T5, T6    | Open   | Future    | Trend Prediction |

---

## 📁 File Output Location

* **Windows:** `C:\Users\<YourUser>\Desktop`
* **macOS / Linux:** `~/Desktop`

---

## 🧪 Testing & Verification

You can verify the component is working by checking the logs upon startup:

```text
Starting Reporting Component (In-Memory Mode)...
ReportingService registered successfully.
```

If you run the `ReportingLauncher` locally (outside OSGi), you should see console output demonstrating:

* Report generation
* File creation
* Failure rate calculation
* Ticket trend forecasting
