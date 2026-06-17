package com.rlogical.automation;

import com.rlogical.automation.modules.Module1_FormFlow;
import com.rlogical.automation.utils.BrowserLauncher;

public class App {
    static {
        // Force SLF4J simple logging completely off
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "off");

        // Extract native libraries to temp directory and set jna.library.path to prioritize them
        try {
            String resourcePrefix = com.sun.jna.Platform.RESOURCE_PREFIX;
            java.io.File tessFolder = net.sourceforge.tess4j.util.LoadLibs.extractTessResources(resourcePrefix);
            java.io.File leptFolder = net.sourceforge.lept4j.util.LoadLibs.extractNativeResources(resourcePrefix);
            String currentPath = System.getProperty("jna.library.path");
            String newPath = "";

            if (tessFolder != null) {
                newPath += tessFolder.getAbsolutePath();
            }
            if (leptFolder != null) {
                if (!newPath.isEmpty()) {
                    newPath += java.io.File.pathSeparator;
                }
                newPath += leptFolder.getAbsolutePath();
            }

            // For macOS, we still want to append Homebrew paths if they are not already there
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("mac")) {
                if (!newPath.isEmpty()) {
                    newPath += java.io.File.pathSeparator;
                }
                newPath += "/opt/homebrew/lib" + java.io.File.pathSeparator + "/usr/local/lib";
            }

            if (!newPath.isEmpty()) {
                if (currentPath != null && !currentPath.isEmpty()) {
                    newPath = newPath + java.io.File.pathSeparator + currentPath;
                }
                System.setProperty("jna.library.path", newPath);
            }
        } catch (Throwable t) {
            System.err.println("Warning: Failed to programmatically extract native libraries: " + t.getMessage());
        }
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
