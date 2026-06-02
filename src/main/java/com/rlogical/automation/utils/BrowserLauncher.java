package com.rlogical.automation.utils;

import com.microsoft.playwright.*;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;

public class BrowserLauncher implements AutoCloseable {
    public final Playwright playwright;
    public final Browser browser;
    public final BrowserContext context;
    public final Page page;
    private final PrintStream originalOut;
    private final PrintStream originalErr;

    static {
        // Temporarily redirect standard streams to suppress Playwright driver startup validation prints during class loading
        PrintStream origOut = System.out;
        PrintStream origErr = System.err;
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));
        System.setErr(new PrintStream(OutputStream.nullOutputStream()));
        try {
            Class.forName("com.microsoft.playwright.Playwright");
            Class.forName("com.microsoft.playwright.impl.driver.Driver");
        } catch (ClassNotFoundException e) {
            // Ignore
        } finally {
            System.setOut(origOut);
            System.setErr(origErr);
        }
    }

    public BrowserLauncher(String browserType, boolean headless) {
        // Redirect streams to suppress console warnings/noise during launch/init
        this.originalOut = System.out;
        this.originalErr = System.err;
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));
        System.setErr(new PrintStream(OutputStream.nullOutputStream()));

        try {
            this.playwright = Playwright.create();
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(headless);

            if ("firefox".equalsIgnoreCase(browserType)) {
                this.browser = playwright.firefox().launch(launchOptions);
            } else if ("webkit".equalsIgnoreCase(browserType)) {
                this.browser = playwright.webkit().launch(launchOptions);
            } else {
                this.browser = playwright.chromium().launch(launchOptions);
            }

            Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                    .setViewportSize(1280, 800);
            if (!"webkit".equalsIgnoreCase(browserType)) {
                contextOptions.setPermissions(Collections.singletonList("notifications"));
            }
            this.context = browser.newContext(contextOptions);
            this.page = context.newPage();
        } catch (Exception e) {
            close();
            throw e;
        } finally {
            // Restore original streams
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
    }

    @Override
    public void close() {
        // Mute streams during closing
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));
        System.setErr(new PrintStream(OutputStream.nullOutputStream()));
        try {
            if (context != null) context.close();
            if (browser != null) browser.close();
            if (playwright != null) playwright.close();
        } catch (Exception e) {
            // Ignore
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
    }
}
