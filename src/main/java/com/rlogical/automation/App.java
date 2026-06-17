package com.rlogical.automation;

import com.rlogical.automation.modules.Module1_FormFlow;
import com.rlogical.automation.utils.BrowserLauncher;

public class App {
    static {
        // Set native library path for JNA depending on the operating system
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            System.setProperty("jna.library.path", "/opt/homebrew/lib:/usr/local/lib");
        } else if (!os.contains("win")) {
            System.setProperty("jna.library.path", "/usr/lib/x86_64-linux-gnu:/usr/lib");
        }
        // Turn off SLF4J simple logging completely
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "off");
    }

    public static void main(String[] args) {
        String browserType = "chromium"; // Always Chromium for exec:java
        boolean success = false;

        // Run visibles single-browser local execution
        try (BrowserLauncher launcher = new BrowserLauncher(browserType, false)) {
            success = Module1_FormFlow.execute(launcher.page, browserType);
        } catch (Exception e) {
            success = false;
        }

        String friendlyName = "chrome";
        if (success) {
            System.out.println("form submitted successfully in " + friendlyName);
        } else {
            System.out.println("form submission failed in " + friendlyName);
        }
    }
}
