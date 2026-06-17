package com.rlogical.automation.utils;

import com.microsoft.playwright.Page;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FailureHandler {

    public interface FlowAction {
        boolean run(Page page) throws Exception;
    }

    public static boolean runWithFailureHandling(Page page, String moduleName, String browserName, FlowAction action) {
        try {
            boolean success = action.run(page);
            if (!success) {
                handleFailure(page, moduleName, browserName, "FlowFailed");
            }
            return success;
        } catch (Exception e) {
            e.printStackTrace(); // Print stack trace to debug failures
            handleFailure(page, moduleName, browserName, "Exception_" + e.getClass().getSimpleName());
            return false;
        }
    }

    public static void handleFailure(Page page, String moduleName, String browserName, String context) {
        try {
            if (page == null) {
                return;
            }

            // Standardize browser name (e.g. chromium -> Chrome)
            String friendlyBrowser = browserName;
            if ("chromium".equalsIgnoreCase(browserName)) {
                friendlyBrowser = "Chrome";
            /*
            } else if ("firefox".equalsIgnoreCase(browserName)) {
                friendlyBrowser = "Firefox";
            } else if ("webkit".equalsIgnoreCase(browserName)) {
                friendlyBrowser = "WebKit";
            */
            }

            // Create directories: screenshots/<module-name>/<friendlyBrowser>/
            String dirPath = "screenshots/" + moduleName + "/" + friendlyBrowser.toLowerCase();
            Files.createDirectories(Paths.get(dirPath));

            // Format timestamp: yyyy-MM-dd_HH-mm-ss
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());

            // Build filename: ModuleName_Browser_Timestamp_Context.png
            String filename = moduleName + "_" + friendlyBrowser + "_" + timestamp + "_" + context + ".png";
            String fullPath = dirPath + "/" + filename;

            // Capture screenshot
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(fullPath)));
        } catch (Exception e) {
            // Ignore failure handler exceptions to ensure they never stop execution
        }
    }
}
