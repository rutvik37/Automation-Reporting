package com.rlogical.automation.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class ExtentReportListener implements ITestListener {

    private static ExtentReports extent;
    private static final ConcurrentHashMap<Long, ExtentTest> testMap = new ConcurrentHashMap<>();
    private static final String REPORT_DIR = "reports";
    private static final String REPORT_FILE = REPORT_DIR + "/AutomationReport.html";

    private static synchronized ExtentReports createExtentInstance() {
        if (extent == null) {
            try {
                Files.createDirectories(Paths.get(REPORT_DIR));
            } catch (IOException e) {
                // Ignore
            }
            ExtentSparkReporter spark = new ExtentSparkReporter(REPORT_FILE);
            spark.config().setTheme(Theme.DARK);
            spark.config().setDocumentTitle("Automation Execution Report");
            spark.config().setReportName("Playwright Java + TestNG — Chrome Browser Execution");
            spark.config().setTimeStampFormat("yyyy-MM-dd HH:mm:ss");

            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Framework", "Playwright Java + TestNG");
            extent.setSystemInfo("Execution Time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        }
        return extent;
    }

    private ExtentTest getCurrentTest() {
        return testMap.get(Thread.currentThread().getId());
    }

    @Override
    public void onStart(ITestContext context) {
        createExtentInstance();
    }

    @Override
    public void onTestStart(ITestResult result) {
        // Extract browser name from TestNG parameter
        String browser = "unknown";
        Object[] params = result.getParameters();
        if (params != null && params.length > 0) {
            browser = params[0].toString();
            if ("chromium".equalsIgnoreCase(browser))
                browser = "Chrome";
            /*
             * else if ("firefox".equalsIgnoreCase(browser))
             * browser = "Firefox";
             * else if ("webkit".equalsIgnoreCase(browser))
             * browser = "WebKit";
             */
        }

        ExtentTest test = createExtentInstance()
                .createTest(result.getMethod().getMethodName() + " [" + browser + "]")
                .assignCategory(browser);
        testMap.put(Thread.currentThread().getId(), test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = getCurrentTest();
        if (test != null) {
            test.log(Status.PASS, "Test PASSED");
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = getCurrentTest();
        if (test != null) {
            test.log(Status.FAIL, "Test FAILED: " + result.getThrowable());
            // Attach failure screenshot if exists
            attachLatestScreenshot(test, result);
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = getCurrentTest();
        if (test != null) {
            test.log(Status.SKIP, "Test SKIPPED");
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        if (extent != null) {
            extent.flush();
        }
        testMap.remove(Thread.currentThread().getId());
    }

    private void attachLatestScreenshot(ExtentTest test, ITestResult result) {
        try {
            // Look in screenshots/<module>/<browser>/ for the most recent screenshot
            File screenshotsRoot = new File("screenshots");
            if (!screenshotsRoot.exists())
                return;

            File latestFile = null;
            for (File moduleDir : screenshotsRoot.listFiles(File::isDirectory)) {
                for (File browserDir : moduleDir.listFiles(File::isDirectory)) {
                    File[] pngs = browserDir.listFiles(f -> f.getName().endsWith(".png"));
                    if (pngs == null)
                        continue;
                    for (File png : pngs) {
                        if (latestFile == null || png.lastModified() > latestFile.lastModified()) {
                            latestFile = png;
                        }
                    }
                }
            }

            if (latestFile != null) {
                byte[] bytes = Files.readAllBytes(latestFile.toPath());
                String base64 = Base64.getEncoder().encodeToString(bytes);
                test.addScreenCaptureFromBase64String(base64, "Failure Screenshot: " + latestFile.getName());
            }
        } catch (Exception e) {
            // Ignore screenshot attachment errors
        }
    }
}
