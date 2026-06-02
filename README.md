# 🤖 Automation Reporting

A high-performance Playwright Java + TestNG automation framework featuring parallel cross-browser execution, Tesseract OCR captcha handling, Extent HTML reporting, and GitHub Actions CI/CD with automatic email reports.

---

## 📋 Table of Contents
- [Project Structure](#-project-structure)
- [Tech Stack](#-tech-stack)
- [Execution Modes](#-execution-modes)
- [Reports & Failure Screenshots](#-reports--failure-screenshots)
- [GitHub Actions & Email Reporting](#-github-actions--email-reporting)
- [Housekeeping & Clean-up](#-housekeeping--clean-up)

---

## 📁 Project Structure

```text
Automation-Reporting/
├── .github/workflows/
│   └── automation.yml          # CI/CD pipeline configuration
├── src/main/java/com/rlogical/automation/
│   ├── App.java                # Single-browser visible entry point (dev)
│   ├── modules/
│   │   └── Module1_FormFlow.java  # Flow: URL → Fill Form → Submit
│   └── utils/
│       ├── AutomationHelper.java  # OCR, forms, popups, and random generators
│       ├── BrowserLauncher.java   # Thread-safe Playwright browser manager
│       └── FailureHandler.java    # Automatic failure screenshot taker
├── src/test/java/com/rlogical/automation/
│   ├── BrowserTest.java        # Parallel browser execution test class
│   └── reporting/
│       └── ExtentReportListener.java # Extent HTML report suite listener
├── tessdata/                   # Required Tesseract OCR training data
├── testng.xml                  # TestNG suite configurations
└── pom.xml                     # Maven dependencies & build setup
```

---

## 🛠 Tech Stack

* **Java 17**: Core programming language.
* **Playwright Java (1.49.0)**: Modern browser automation.
* **TestNG (7.10.2)**: Thread-safe parallel test runner.
* **Tess4J (5.18.0) / Tesseract OCR**: Captcha text recognition.
* **ExtentReports (5.1.1)**: Dark-theme test execution reports.

---

## ⚡ Execution Modes

### 1. Local Development (Single Browser, Visible)
Useful for local debugging and script creation. Runs Chrome only.
```bash
mvn compile exec:java
```

### 2. Full Parallel Test Suite (Multi-Browser, Headless/Visible)
Runs tests in Chromium, Firefox, and WebKit simultaneously.
```bash
mvn clean test
```

---

## 📊 Reports & Failure Screenshots

* **Extent HTML Report**: Generated automatically after every run at `reports/AutomationReport.html`. Displays logs categorized by browser.
* **Failure Screenshots**: Captured automatically on any test failure. Screenshots are:
  * Embedded directly into the HTML report.
  * Saved under `screenshots/{ModuleName}/{BrowserName}/{Filename}.png`.

---

## 🚀 GitHub Actions & Email Reporting

Every push to `master` (or manual run from the GitHub Actions tab) triggers the CI/CD pipeline:
1. Installs Java 17, Tesseract OCR, and Playwright browsers on the virtual runner.
2. Runs the parallel test suite (`mvn clean test`).
3. Uploads the HTML report and screenshots as run artifacts.
4. Sends a styled email execution report to the configured receiver.

### GitHub Secrets Setup (Required for Email Reports)
Add these secrets in `GitHub Repository -> Settings -> Secrets and variables -> Actions`:

| Secret Name | Purpose | Example / How-to |
|---|---|---|
| `EMAIL_USERNAME` | Sender email address | `sender@gmail.com` |
| `EMAIL_PASSWORD` | Gmail App Password | 16-character code from Google Security settings |
| `RECEIVER_EMAIL` | Email address to receive the report | `recipient@gmail.com` |

---

## 🧹 Housekeeping & Clean-up

To keep the repository light and clean, manage your files as follows:

| Directory / File | Description | Action / Recommendation |
|---|---|---|
| **`tessdata/`** | Contains Tesseract language files. | **DO NOT REMOVE** (Required for captcha solver). |
| **`scratch/`** | Temporary OCR screenshot folder. | **DO NOT REMOVE** (The code automatically creates and cleans up temporary files after every run). |
| **`screenshots/`** | Failure screenshots folder. | **SAFE TO DELETE** (Can be cleared manually anytime). |
| **`reports/`** | Test execution reports. | **SAFE TO DELETE** (Regenerated automatically on the next run). |
| **`target/`** | Compiled class files and Maven build output. | **SAFE TO DELETE** (Run `mvn clean` to delete it). |
