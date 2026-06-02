# 🤖 Automation Reporting

> Enterprise-grade Playwright Java + TestNG automation framework with parallel cross-browser execution, Tesseract OCR captcha handling, Extent HTML reporting, GitHub Actions CI/CD, and automated email notifications.

---

## 📋 Table of Contents
- [Project Structure](#project-structure)
- [Tech Stack](#tech-stack)
- [Local Execution](#local-execution)
- [TestNG Parallel Execution](#testng-parallel-execution)
- [GitHub Actions CI/CD](#github-actions-cicd)
- [Reports](#reports)
- [GitHub Secrets Setup](#github-secrets-setup)
- [Adding New Modules](#adding-new-modules)

---

## 📁 Project Structure

```
Automation Reporting/
├── .github/
│   └── workflows/
│       └── automation.yml          # GitHub Actions CI pipeline
├── src/
│   ├── main/java/com/rlogical/automation/
│   │   ├── App.java                # Entry point for exec:java mode
│   │   ├── modules/
│   │   │   └── Module1_FormFlow.java  # Module 1: URL → Fill Form → Submit
│   │   └── utils/
│   │       ├── AutomationHelper.java  # Captcha OCR, screenshot, element helpers
│   │       └── BrowserLauncher.java   # Browser factory (chromium/firefox/webkit)
│   └── test/java/com/rlogical/automation/
│       ├── BrowserTest.java            # TestNG parallel test entry
│       └── reporting/
│           └── ExtentReportListener.java  # Extent HTML report generator
├── tessdata/                       # Tesseract OCR training data
├── screenshots/                    # Auto-captured failure screenshots
│   └── {Module}/{Browser}/{timestamp}.png
├── reports/
│   └── AutomationReport.html       # Generated Extent HTML report
├── testng.xml                      # TestNG suite configuration
├── pom.xml                         # Maven build + dependencies
└── README.md
```

---

## 🛠 Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Core language |
| Playwright Java | 1.49.0 | Browser automation |
| TestNG | 7.10.2 | Test framework + parallel execution |
| Tess4J | 5.18.0 | Tesseract OCR — captcha reading |
| ExtentReports | 5.1.1 | HTML execution reports |
| Maven | 3.9+ | Build + dependency management |

---

## ⚡ Local Execution

### Single Browser (Development / Debug Mode)
```bash
mvn compile exec:java
```
- Runs in **Chromium** (visible browser window)
- Executes current module flow: **URL → Fill Form → Submit**
- Browser closes automatically after execution

### Parallel Cross-Browser Execution
```bash
mvn clean test
```
- Runs all 3 browsers **simultaneously**: Chromium + Firefox + WebKit
- Uses TestNG parallel configuration from `testng.xml`
- One browser failure does **not** stop others
- Generates `reports/AutomationReport.html` after execution

---

## 🌐 TestNG Parallel Execution

The `testng.xml` configures true parallel browser execution:

```xml
<suite name="AutomationSuite" parallel="tests" thread-count="3" verbose="0">
  <test name="Chrome">  → Chromium
  <test name="Firefox"> → Firefox
  <test name="WebKit">  → WebKit (Safari engine)
</suite>
```

Each browser runs in its own thread with isolated Playwright instances (thread-safe).

---

## 🚀 GitHub Actions CI/CD

The pipeline (`.github/workflows/automation.yml`) automatically triggers on:
- Every **push** to `main` or `master`
- Manual trigger via **GitHub → Actions → Run workflow**

### Pipeline Steps
1. ✅ Checkout code
2. ✅ Set up Java 17 (Temurin)
3. ✅ Install Tesseract OCR
4. ✅ Install Playwright browsers (with system deps)
5. ✅ Run `mvn clean test` (all 3 browsers in parallel)
6. ✅ Upload **HTML report** as GitHub artifact (retained 30 days)
7. ✅ Upload **failure screenshots** as GitHub artifact
8. ✅ Send **email report** with pass/fail summary + attached HTML report

---

## 📊 Reports

### Extent HTML Report
After running `mvn clean test`, open the report in your browser:
```
reports/AutomationReport.html
```

**Report Features:**
- 🌑 Dark theme
- Browser-wise test categorization (Chrome / Firefox / WebKit)
- Pass ✅ / Fail ❌ / Skip ⏭ status per test
- Auto-embedded failure screenshots
- Execution timestamp and system info

---

## 🔐 GitHub Secrets Setup

To enable automated email reports, configure these **3 secrets** in your GitHub repository:

**Go to:** `GitHub Repository → Settings → Secrets and variables → Actions → New repository secret`

| Secret Name | Description | Example |
|---|---|---|
| `EMAIL_USERNAME` | Gmail address used to send reports | `yourname@gmail.com` |
| `EMAIL_PASSWORD` | Gmail **App Password** (not your regular password) | `abcd efgh ijkl mnop` |
| `RECEIVER_EMAIL` | Email address to receive the report | `team@company.com` |

### How to Generate a Gmail App Password
1. Go to [Google Account Security](https://myaccount.google.com/security)
2. Enable **2-Step Verification** (required)
3. Go to **App passwords**
4. Select app: **Mail**, device: **Other (custom name)** → type "Automation CI"
5. Copy the 16-character app password → use as `EMAIL_PASSWORD` secret

> [!IMPORTANT]
> Never commit credentials directly in code or workflow files. Always use GitHub Secrets.

---

## 🧩 Adding New Modules

To add a new automation module:

1. Create `src/main/java/com/rlogical/automation/modules/Module2_XYZ.java`
2. Implement your flow using `AutomationHelper` utilities
3. Call it from `BrowserTest.java` inside the `runTest()` method
4. For local dev mode, call it from `App.java`

---

## 📸 Failure Screenshots

Screenshots are automatically captured on any failure and saved to:
```
screenshots/
└── {ModuleName}/
    └── {BrowserName}/
        └── {ModuleName}_{Browser}_{timestamp}.png
```

Screenshots are also:
- 📎 Embedded in the Extent HTML report
- 📤 Uploaded as GitHub Actions artifacts

---

*Generated and maintained by the Antigravity automation framework.*
