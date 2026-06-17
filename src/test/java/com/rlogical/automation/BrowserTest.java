package com.rlogical.automation;

import com.rlogical.automation.modules.Module1_FormFlow;
import com.rlogical.automation.modules.Module2_ContactFormFlow;
import com.rlogical.automation.utils.BrowserLauncher;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class BrowserTest {
    static {
        // Set native library path for JNA to find Homebrew's libtesseract on macOS
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            System.setProperty("jna.library.path", "/opt/homebrew/lib:/usr/local/lib");
        }
        // Turn off SLF4J simple logging completely
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "off");
    }

    @Parameters("browser")

    @Test
    public void runBrowserTest(String browserName) {
        boolean success = false;

        // Run full parallel execution (headless=false / visible, as per request)
        try (BrowserLauncher launcher = new BrowserLauncher(browserName, false)) {
            // Execute Module 1. Future modules can be appended here.
            success = Module1_FormFlow.execute(launcher.page, browserName);
        } catch (Exception e) {
            e.printStackTrace(); // Print launch exceptions to console for visibility
            success = false;
        }

        String friendlyName = browserName;
        if ("chromium".equalsIgnoreCase(browserName)) {
            friendlyName = "chrome";
        }

        if (success) {
            System.out.println("form submitted successfully in " + friendlyName.toLowerCase());
        } else {
            System.out.println("form submission failed in " + friendlyName.toLowerCase());
        }

        org.testng.Assert.assertTrue(success, "Form submission failed in " + friendlyName.toLowerCase());
    }

    @Parameters("browser")
    @Test
    public void runBrowserTest2(String browserName) {
        boolean success = false;

        try (BrowserLauncher launcher = new BrowserLauncher(browserName, false)) {
            // Execute Module 2.
            success = Module2_ContactFormFlow.execute(launcher.page, browserName);
        } catch (Exception e) {
            e.printStackTrace(); // Print launch exceptions to console for visibility
            success = false;
        }

        String friendlyName = browserName;
        if ("chromium".equalsIgnoreCase(browserName)) {
            friendlyName = "chrome";
        }

        if (success) {
            System.out.println("contact form submitted successfully in " + friendlyName.toLowerCase());
        } else {
            System.out.println("contact form submission failed in " + friendlyName.toLowerCase());
        }

        org.testng.Assert.assertTrue(success, "Contact form submission failed in " + friendlyName.toLowerCase());
    }
}
