# 📊 Reporting Component (OSGi Bundle)

## Overview

The **Reporting Component** provides analytics and data export capabilities for the Helpdesk System. It aggregates ticket data to generate performance reports, forecast future workload, and analyze service failures.

> ⚠️ **ARCHITECTURAL NOTE**
> To ensure stability and avoid dependency conflicts, this component runs in **In-Memory Mode** with **Smart Mock Data**.
>
> * Utilizes the **real `Ticket` entity** structure from `base-library`
> * Internally creates specific ticket scenarios (e.g., SLA breaches) to demonstrate logic
> * Generated report files persist on disk, but the **report archive list resets on restart**

---

## ✅ Implemented Features & Logic

### 1. Manage Saved Reports (CRUD)

* **Create / Read / Update / Delete:** Full lifecycle management for report archives
* **Storage:** Reports are stored in an internal **thread-safe list**

---

### 2. Custom Data Export (Robust File Handling)

* **Function:** Exports reports to physical files on the user's machine
* **Smart Path Detection:**

    1. Attempts to save to `Desktop/`
    2. If Desktop is unavailable (e.g., OneDrive issues), falls back to **User Home Directory**
       (`C:\Users\<Name>\`)
* **Supported Formats:**

    * **CSV** – Detailed structured data
    * **Text Summary** – Human-readable analytics output

---

### 3. Failure Rate Analysis (SLA Logic)

Calculates failure rates using **Service Level Agreement (SLA)** rules applied to mock tickets:

| Priority     | SLA Requirement             |
| ------------ | --------------------------- |
| URGENT       | Resolve within **24 hours** |
| HIGH         | Resolve within **48 hours** |
| MEDIUM / LOW | Resolve within **7 days**   |

**Output Includes:**

* Total tickets analyzed
* Number of **SLA breaches**
* Calculated failure percentage
* Overall **Risk Level** (`CRITICAL` / `STABLE`)

---

### 4. Forecast Ticket Trends (Statistical Analysis)

Predicts next week's ticket volume using **Least Squares Linear Regression**.

* **Data Range:** Week -3 to Current Week
* **Method:** Line of best fit calculation
* **Formula:**
  [ y = mx + c ]
* **Result:** Mathematical prediction for **Week +1** ticket volume

---

## 🛠️ Installation & Deployment

### 1. Prerequisites

Ensure `base-library` is built and installed first, as this component depends on the real `Ticket` entity.

```bash
cd base-library
mvn clean install
```

---

### 2. Build Reporting Component

```bash
cd reporting-component
mvn clean install
```

---

### 3. Deploy to Apache Felix

Start Apache Felix:

```bash
java -jar bin/felix.jar
```

Install and start the bundle via the **g! console**:

```bash
# Install bundle
install file:///path/to/reporting-component/target/reporting-component-1.0.0.jar

# Start bundle
start <BUNDLE_ID>
```

---

## 🔌 Service API Usage

**Interface:**

```
com.um.helpdesk.service.ReportingService
```

### Examples

#### 1. Generate & Save a Report

```java
ReportingService service = ...; // Obtained via @Reference

SavedReportArchive report = service.generateReport("Monthly_Stats", "CSV");
System.out.println("File saved at: " + report.getFilePath());
```

---

#### 2. Get Failure Analysis

```java
Map<String, Object> analysis = service.getFailureRateAnalysis();

System.out.println("Risk Level: " + analysis.get("Risk Level"));
// Example Output: CRITICAL (failure rate > 15%)
```

---

#### 3. Get Trend Forecast

```java
Map<String, Integer> trends = service.getTicketTrendForecast();

System.out.println("Forecast: " + trends.get("Week 5 (Forecast)"));
```

---

## 📂 Internal Data Scenarios

The component initializes with structured mock tickets to validate logic:

| Scenario          | Priority | Status | Timeframe   | Outcome                  |
| ----------------- | -------- | ------ | ----------- | ------------------------ |
| Critical DB Error | URGENT   | Closed | Last Week   | ❌ Failure (3 days > 24h) |
| System Outage     | URGENT   | Open   | Current     | ❌ Failure (>48h)         |
| WiFi Down         | MEDIUM   | Closed | 3 Weeks Ago | ✅ Success                |
| VPN Access        | HIGH     | Closed | Last Week   | ✅ Success                |

* **Total Tickets:** 11
* **Expected Failure Rate:** ~18% (2 / 11)
* **Trend Pattern:** `2 → 3 → 4 → 2 (Current)`
* **Predicted Next Week:** **5 tickets**

---

## 🧪 Verification

### OSGi Console Logs

```text
Starting Reporting Component (In-Memory Mode)...
ReportingService registered successfully.
```

### ReportingLauncher Output (Local Testing)

```text
✅ FILE CREATED: C:\Users\...\Desktop\Visual_Trend_....csv
Failure Stats: { ..., Risk Level=CRITICAL }
Trends: { ..., Week 5 (Forecast)=5 }
```
