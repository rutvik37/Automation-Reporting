package com.rlogical.automation;

import com.rlogical.automation.modules.Module1_FormFlow;
import com.rlogical.automation.utils.BrowserLauncher;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class BrowserTest {

    @Parameters("browser")
    @Test
    public void runBrowserTest(String browserName) {
        boolean success = false;

        // Run full parallel execution (headless=false / visible, as per existing config)
        try (BrowserLauncher launcher = new BrowserLauncher(browserName, false)) {
            // Execute Module 1. Future modules can be appended here.
            success = Module1_FormFlow.execute(launcher.page, browserName);
        } catch (Exception e) {
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
    }
}
