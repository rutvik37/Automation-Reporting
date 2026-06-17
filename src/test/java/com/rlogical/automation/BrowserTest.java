package com.rlogical.automation;

import com.rlogical.automation.modules.Module1_FormFlow;
import com.rlogical.automation.modules.Module2_ContactFormFlow;
import com.rlogical.automation.utils.BrowserLauncher;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class BrowserTest {
    static {
        // Force SLF4J simple logging completely off
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "off");

        // Extract native libraries to temp directory and set jna.library.path to prioritize them
        try {
            String resourcePrefix = com.sun.jna.Platform.RESOURCE_PREFIX;
            java.io.File tessFolder = net.sourceforge.tess4j.util.LoadLibs.extractTessResources(resourcePrefix);
            java.io.File leptFolder = net.sourceforge.lept4j.util.LoadLibs.extractNativeResources(resourcePrefix);
            String currentPath = System.getProperty("jna.library.path");
            String newPath = tessFolder.getAbsolutePath() + java.io.File.pathSeparator + leptFolder.getAbsolutePath();

            // For macOS, we still want to append Homebrew paths if they are not already there
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("mac")) {
                newPath = newPath + java.io.File.pathSeparator + "/opt/homebrew/lib" + java.io.File.pathSeparator + "/usr/local/lib";
            }

            if (currentPath != null && !currentPath.isEmpty()) {
                newPath = newPath + java.io.File.pathSeparator + currentPath;
            }
            System.setProperty("jna.library.path", newPath);
        } catch (Throwable t) {
            System.err.println("Warning: Failed to programmatically extract native libraries: " + t.getMessage());
        }
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
