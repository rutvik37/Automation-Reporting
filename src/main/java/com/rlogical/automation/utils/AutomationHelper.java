package com.rlogical.automation.utils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AutomationHelper {
    static {
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
    private static final String TESSDATA_PATH = "tessdata";
    public static boolean enablePopupWatcher = false;

    // Static form data values
    private static final String STATIC_NAME = "Tester Testing";
    private static final String STATIC_EMAIL = "testing5555@gmail.com";
    private static final String STATIC_MOBILE = "+91 1234567890";
    private static final String STATIC_COMPANY = "Testing company";
    private static final String STATIC_DESCRIPTION = "Testing Decription for verify these functionality";

    public static java.util.List<String> getMissingFields(Locator container) {
        java.util.List<String> missingFields = new java.util.ArrayList<>();

        Locator fnameField = container.locator("input[name='fname']").first();
        if (fnameField.count() == 0 || fnameField.inputValue().trim().isEmpty()) {
            missingFields.add("Name");
        }

        Locator emailField = container.locator("input[name='email']").first();
        if (emailField.count() == 0 || emailField.inputValue().trim().isEmpty()) {
            missingFields.add("Email");
        }

        Locator mobileField = container.locator("input[name='mobile']").first();
        if (mobileField.count() == 0 || mobileField.inputValue().trim().isEmpty()) {
            missingFields.add("Phone");
        }

        Locator companyField = container.locator("input[name='company']").first();
        if (companyField.count() == 0 || companyField.inputValue().trim().isEmpty()) {
            missingFields.add("Company");
        }

        Locator describeField = container.locator("textarea[name='describe']").first();
        if (describeField.count() == 0 || describeField.inputValue().trim().isEmpty()) {
            missingFields.add("Project Details");
        }

        Locator captchaInput = container.locator("input.wpcf7-captchar, input[placeholder*='captcha'], input[name^='captcha-']").first();
        if (captchaInput.count() == 0 || captchaInput.inputValue().trim().isEmpty()) {
            missingFields.add("Captcha");
        }

        return missingFields;
    }

    public static void fillMissingFields(Page page, Locator container, java.util.List<String> missingFields, String browserType) {
        if (missingFields == null || missingFields.isEmpty()) {
            return;
        }

        for (String field : missingFields) {
            switch (field) {
                case "Name":
                    Locator fnameField = container.locator("input[name='fname']").first();
                    safeWaitFor(page, fnameField, new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
                    safeFill(page, fnameField, STATIC_NAME);
                    break;
                case "Email":
                    Locator emailField = container.locator("input[name='email']").first();
                    safeWaitFor(page, emailField, new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
                    safeFill(page, emailField, STATIC_EMAIL);
                    break;
                case "Phone":
                    Locator mobileField = container.locator("input[name='mobile']").first();
                    safeWaitFor(page, mobileField, new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
                    String cleanedMobile = STATIC_MOBILE.replaceAll("[^0-9]", "");
                    if (cleanedMobile.length() > 10) {
                        cleanedMobile = cleanedMobile.substring(cleanedMobile.length() - 10);
                    }
                    safeFill(page, mobileField, cleanedMobile);
                    break;
                case "Company":
                    Locator companyField = container.locator("input[name='company']").first();
                    safeWaitFor(page, companyField, new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
                    safeFill(page, companyField, STATIC_COMPANY);
                    break;
                case "Project Details":
                    Locator describeField = container.locator("textarea[name='describe']").first();
                    safeWaitFor(page, describeField, new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
                    safeFill(page, describeField, STATIC_DESCRIPTION);
                    break;
                case "Captcha":
                    Locator captchaImg = container.locator("img.wpcf7-captchac, img[alt='captcha'], img[src*='captcha']").first();
                    Locator captchaInput = container.locator("input.wpcf7-captchar, input[placeholder*='captcha'], input[name^='captcha-']").first();
                    solveCaptchaForForm(page, container, captchaImg, captchaInput, browserType);
                    break;
            }
        }
    }

    public static boolean fillAndSubmitForm(Page page, String containerSelector, String browserType) {
        Locator container = page.locator(containerSelector).first();

        try {
            safeWaitFor(page, container, new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
        } catch (Exception e) {
            System.err.println("[" + browserType + "] Error: Form container " + containerSelector + " not visible.");
            return false;
        }

        Locator captchaImg = container.locator("img.wpcf7-captchac, img[alt='captcha'], img[src*='captcha']").first();
        Locator captchaInput = container
                .locator("input.wpcf7-captchar, input[placeholder*='captcha'], input[name^='captcha-']").first();

        try {
            safeWaitFor(page, captchaImg, new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(15000));
            safeWaitFor(page, captchaInput, new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(15000));
        } catch (Exception e) {
            System.err.println("[" + browserType + "] Error: Captcha image or input element not found in form.");
            return false;
        }

        int maxSubmitAttempts = 3;
        String lastChallengeId = "";
        String lastImgSrc = "";

        try {
            for (int submitAttempt = 1; submitAttempt <= maxSubmitAttempts; submitAttempt++) {
                if (containerSelector.equals("#quickContact")) {
                    if (submitAttempt == 1) {
                        fillDummyDataForForm(page, container);
                        solveCaptchaForForm(page, container, captchaImg, captchaInput, browserType);

                        int validationAttempts = 0;
                        int maxValidationAttempts = 5;
                        java.util.List<String> missingFields = getMissingFields(container);

                        if (!missingFields.isEmpty()) {
                            System.out.println("Missing mandatory fields:");
                            for (String field : missingFields) {
                                System.out.println("* " + field);
                            }
                            System.out.println();
                            System.out.println("Refilling missing fields only.");

                            while (!missingFields.isEmpty() && validationAttempts < maxValidationAttempts) {
                                fillMissingFields(page, container, missingFields, browserType);
                                missingFields = getMissingFields(container);
                                validationAttempts++;
                            }
                        }

                        if (missingFields.isEmpty()) {
                            System.out.println("All mandatory fields verified successfully.");
                        } else {
                            System.err.println("[" + browserType + "] Error: Mandatory fields not filled successfully after validation.");
                            return false;
                        }
                    } else {
                        System.out.println("Submission failed.");
                        System.out.println("Rechecking field values.");
                        System.out.println();

                        // Wait for new captcha
                        long refreshStart = System.currentTimeMillis();
                        while (System.currentTimeMillis() - refreshStart < 5000) { // max 5 seconds wait for refresh
                            String currentChallengeId = getCaptchaChallengeId(container);
                            String currentImgSrc = "";
                            try {
                                currentImgSrc = captchaImg.getAttribute("src");
                            } catch (Exception e) {
                                // Ignore
                            }
                            if ((currentChallengeId != null && !currentChallengeId.equals(lastChallengeId))
                                    || (currentImgSrc != null && !currentImgSrc.equals(lastImgSrc))) {
                                break;
                            }
                            page.waitForTimeout(100);
                        }
                        // Give a tiny buffer for image to load/render
                        page.waitForTimeout(200);

                        // Clear captcha input to force solving a fresh captcha
                        try {
                            safeFill(page, captchaInput, "");
                        } catch (Exception e) {
                            // Ignore
                        }

                        java.util.List<String> missingFields = getMissingFields(container);

                        System.out.println("Refilling:");
                        for (String field : missingFields) {
                            System.out.println("* " + field);
                        }
                        System.out.println();

                        // Fill missing fields only
                        int validationAttempts = 0;
                        int maxValidationAttempts = 5;
                        while (!missingFields.isEmpty() && validationAttempts < maxValidationAttempts) {
                            fillMissingFields(page, container, missingFields, browserType);
                            missingFields = getMissingFields(container);
                            validationAttempts++;
                        }

                        System.out.println("Retrying submission.");
                    }
                } else {
                    // Legacy non-Module 1 form flow
                    if (submitAttempt == 1) {
                        fillDummyDataForForm(page, container);
                    } else {
                        // Wait for new captcha
                        long refreshStart = System.currentTimeMillis();
                        while (System.currentTimeMillis() - refreshStart < 5000) {
                            String currentChallengeId = getCaptchaChallengeId(container);
                            String currentImgSrc = "";
                            try {
                                currentImgSrc = captchaImg.getAttribute("src");
                            } catch (Exception e) {
                                // Ignore
                            }
                            if ((currentChallengeId != null && !currentChallengeId.equals(lastChallengeId))
                                    || (currentImgSrc != null && !currentImgSrc.equals(lastImgSrc))) {
                                break;
                            }
                            page.waitForTimeout(100);
                        }
                        page.waitForTimeout(200);
                        fillDummyDataForForm(page, container);
                    }

                    boolean captchaSolved = solveCaptchaForForm(page, container, captchaImg, captchaInput, browserType);
                    if (!captchaSolved) {
                        System.err.println("[" + browserType + "] Error: Captcha solving failed.");
                        return false;
                    }
                }

                Locator submitBtn = container.locator("input[type='submit']").first();
                try {
                    safeWaitFor(page, submitBtn, new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
                } catch (Exception e) {
                    System.err.println("[" + browserType + "] Error: Submit button not visible.");
                    return false;
                }

                // Store current captcha challenge ID and image src before clicking submit
                lastChallengeId = getCaptchaChallengeId(container);
                try {
                    lastImgSrc = captchaImg.getAttribute("src");
                } catch (Exception e) {
                    // Ignore
                }

                // Clear response output before submitting to avoid reading old validation messages
                try {
                    page.evaluate("() => { " +
                            "const output = document.querySelector('div.wpcf7-response-output');" +
                            "if (output) { output.style.display = 'none'; output.innerText = ''; }" +
                            "}");
                } catch (Exception e) {
                    // Ignore
                }

                // Log URL and submit button state before clicking submit
                System.out.println("[" + browserType + "] URL before submit: " + page.url());
                System.out.println("[" + browserType + "] Submit button state: visible=" + submitBtn.isVisible() + ", enabled=" + submitBtn.isEnabled());

                if (submitAttempt == 1) {
                    page.waitForTimeout(500); // Wait 0.5 seconds on first attempt
                } else {
                    page.waitForTimeout(100); // Wait 0.1 seconds on retry attempts
                }
                safeClick(page, submitBtn);

                // Quick, responsive poll for success (URL redirect)
                boolean isSuccess = false;

                long startTime = System.currentTimeMillis();
                long maxWaitMs = 20000; // max 20 seconds wait for redirection response

                while (System.currentTimeMillis() - startTime < maxWaitMs) {
                    try {
                        String currentUrl = page.url();
                        if (currentUrl.startsWith("https://uat.rlogical.com/thank-you/") || currentUrl.equals("https://uat.rlogical.com/thank-you")) {
                            isSuccess = true;
                            break;
                        }

                        // Early Failure Detection: check native class states on form element
                        Locator formEl = container;
                        if (!container.evaluate("el => el.tagName").equals("FORM")) {
                            if (container.locator("form").count() > 0) {
                                formEl = container.locator("form").first();
                            } else {
                                try {
                                    Locator ancestorForm = container.locator("xpath=./ancestor::form").first();
                                    if (ancestorForm.count() > 0) {
                                        formEl = ancestorForm;
                                    }
                                } catch (Exception e) {
                                    // Ignore
                                }
                            }
                        }
                        String formClass = (String) formEl.evaluate("el => el.className");
                        if (formClass != null && (formClass.contains("invalid") || formClass.contains("failed") || formClass.contains("spam"))) {
                            System.out.println("[" + browserType + "] Early failure detected via form class: " + formClass);
                            break;
                        }

                        // Early Failure Detection: check for validation tips
                        Locator validationTips = container.locator(".wpcf7-not-valid-tip");
                        if (validationTips.count() > 0) {
                            boolean hasVisibleTip = false;
                            for (int i = 0; i < validationTips.count(); i++) {
                                if (validationTips.nth(i).isVisible()) {
                                    hasVisibleTip = true;
                                    break;
                                }
                            }
                            if (hasVisibleTip) {
                                System.out.println("[" + browserType + "] Early failure detected via validation tips.");
                                break;
                            }
                        }

                        // Early Failure Detection: check for error in response output
                        Locator responseOutput = container.locator("div.wpcf7-response-output").first();
                        if (responseOutput.count() == 0 || !responseOutput.isVisible()) {
                            try {
                                Locator ancestorForm = container.locator("xpath=./ancestor::form").first();
                                if (ancestorForm.count() > 0) {
                                    responseOutput = ancestorForm.locator("div.wpcf7-response-output").first();
                                }
                            } catch (Exception e) {
                                // Ignore
                            }
                        }
                        if (responseOutput.count() > 0 && responseOutput.isVisible()) {
                            String responseText = responseOutput.innerText().trim();
                            if (!responseText.isEmpty() && !responseText.toLowerCase().contains("thank you") && 
                                !responseText.toLowerCase().contains("sent") && !responseText.toLowerCase().contains("success")) {
                                System.out.println("[" + browserType + "] Early failure detected via response output: " + responseText);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        // Ignore exceptions caused by page unloading/navigating during submit
                    }
                    page.waitForTimeout(200);
                }

                // Log URL and success/error details after attempt
                System.out.println("[" + browserType + "] URL after submit: " + page.url());
                if (isSuccess) {
                    System.out.println("[" + browserType + "] Success detected! Redirected to thank you page.");
                } else {
                    System.err.println("[" + browserType + "] Failure detected! Did not redirect to https://uat.rlogical.com/thank-you/");
                }

                // Take screenshot after submit
                try {
                    new File("screenshots").mkdirs();
                    String screenshotName = "screenshots/after_submit_" + browserType + "_attempt_" + submitAttempt + ".png";
                    page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(screenshotName)));
                    System.out.println("[" + browserType + "] Saved screenshot: " + screenshotName);
                } catch (Exception e) {
                    System.err.println("[" + browserType + "] Failed to take screenshot: " + e.getMessage());
                }

                if (isSuccess) {
                    return true;
                }

                System.err.println("[" + browserType + "] Attempt " + submitAttempt + " timed out or failed waiting for redirection.");
                continue;
            }
        } finally {
            cleanUpTempFiles(browserType);
        }
        return false;
    }

    private static void cleanUpTempFiles(String browserType) {
        String rawPath = "scratch/captcha_raw_" + browserType + ".png";
        String preprocessedPath = "scratch/captcha_preprocessed_" + browserType + ".png";
        try {
            Files.deleteIfExists(Paths.get(rawPath));
        } catch (Exception e) {
            // Ignore
        }
        try {
            Files.deleteIfExists(Paths.get(preprocessedPath));
        } catch (Exception e) {
            // Ignore
        }
        try {
            Files.deleteIfExists(Paths.get("scratch/captcha_raw.png"));
            Files.deleteIfExists(Paths.get("scratch/captcha_preprocessed.png"));
            Files.deleteIfExists(Paths.get("scratch/flow_0_loaded.png"));
            Files.deleteIfExists(Paths.get("scratch/flow_1_after_click.png"));
        } catch (Exception e) {
            // Ignore
        }
    }

    public static boolean solveCaptchaForForm(Page page, Locator container, Locator captchaImg, Locator captchaInput, String browserType) {
        int maxCaptchaAttempts = 5;
        String rawPath = "scratch/captcha_raw_" + browserType + ".png";
        String preprocessedPath = "scratch/captcha_preprocessed_" + browserType + ".png";

        for (int attempt = 1; attempt <= maxCaptchaAttempts; attempt++) {
            try {
                safeWaitFor(page, captchaImg, new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
                safeWaitFor(page, captchaInput, new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            } catch (Exception e) {
                return false;
            }

            String initialChallenge = getCaptchaChallengeId(container);
            String extractedCode = tryReadCaptchaFromDOM(captchaImg);

            if (extractedCode == null || extractedCode.trim().isEmpty()) {
                try {
                    Files.createDirectories(Paths.get("scratch"));
                    checkAndDismissPopup(page);
                    captchaImg.screenshot(new Locator.ScreenshotOptions().setPath(Paths.get(rawPath)));

                    String rawText = doTesseractOCR(rawPath);
                    extractedCode = cleanCaptchaText(rawText);

                    if (!isValidCaptcha(extractedCode)) {
                        preprocessImage(rawPath, preprocessedPath, 150);
                        rawText = doTesseractOCR(preprocessedPath);
                        extractedCode = cleanCaptchaText(rawText);
                    }

                    if (!isValidCaptcha(extractedCode)) {
                        preprocessImage(rawPath, preprocessedPath, 120);
                        rawText = doTesseractOCR(preprocessedPath);
                        extractedCode = cleanCaptchaText(rawText);
                    }

                    if (!isValidCaptcha(extractedCode)) {
                        preprocessImage(rawPath, preprocessedPath, 180);
                        rawText = doTesseractOCR(preprocessedPath);
                        extractedCode = cleanCaptchaText(rawText);
                    }

                    if (!isValidCaptcha(extractedCode)) {
                        extractedCode = "1234";
                    }
                } catch (Exception e) {
                    System.err.println("[" + browserType + "] OCR attempt failed: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            String postChallenge = getCaptchaChallengeId(container);
            if (!initialChallenge.equals(postChallenge)) {
                page.waitForTimeout(1000);
                continue;
            }

            safeFill(page, captchaInput, extractedCode);
            page.waitForTimeout(200); // Wait 200ms after filling captcha to register value

            String enteredVal = captchaInput.inputValue();
            String finalChallenge = getCaptchaChallengeId(container);

            if (!initialChallenge.equals(finalChallenge)) {
                page.waitForTimeout(1000);
                continue;
            }

            if (extractedCode.equalsIgnoreCase(enteredVal)) {
                return true;
            } else {
                if (attempt < maxCaptchaAttempts) {
                    refreshCaptchaElement(page);
                }
            }
        }
        return false;
    }

    public static void fillDummyDataForForm(Page page, Locator container) {
        // Name Field
        Locator nameField = container.locator("[data-name='your-name'] input, input[name='your-name'], input[name='fname'], [data-name='fname'] input").first();
        if (nameField.count() > 0) {
            safeFill(page, nameField, STATIC_NAME);
        }

        // Email Field
        Locator emailField = container.locator("input[name='your-email'], [data-name='your-email'] input, input[name='email'], [data-name='email'] input").first();
        if (emailField.count() > 0) {
            safeFill(page, emailField, STATIC_EMAIL);
        }

        // Phone Field (supporting visible intl-tel-input, mobile, and hidden fallback)
        Locator visiblePhone = container.locator("[data-name='your-number'] input, input[type='tel'], input[name='mobile']").first();
        Locator hiddenPhone = container.locator("input[name='full-phone']").first();
        String cleanedMobile = STATIC_MOBILE.replaceAll("[^0-9]", "");
        if (cleanedMobile.length() > 10) {
            cleanedMobile = cleanedMobile.substring(cleanedMobile.length() - 10);
        }
        if (visiblePhone.count() > 0) {
            safeFill(page, visiblePhone, cleanedMobile);
        } else if (hiddenPhone.count() > 0) {
            if (hiddenPhone.isVisible()) {
                safeFill(page, hiddenPhone, cleanedMobile);
            } else {
                checkAndDismissPopup(page);
                hiddenPhone.evaluate("el => { el.value = '" + cleanedMobile + "'; el.dispatchEvent(new Event('change', { bubbles: true })); }");
            }
        }

        // Company Field
        Locator companyField = container.locator("input[name='company'], input[name='your-company']").first();
        if (companyField.count() > 0) {
            safeFill(page, companyField, STATIC_COMPANY);
        }

        // Category Select Dropdown
        Locator categoryField = container.locator("select[name='category'], select[name='your-category']").first();
        if (categoryField.count() > 0 && categoryField.isVisible()) {
            safeSelectOption(page, categoryField, "Hire Developers");
            try {
                checkAndDismissPopup(page);
                categoryField.evaluate("el => el.dispatchEvent(new Event('change', { bubbles: true }))");
            } catch (Exception e) {
                // Ignore
            }
        }

        // Message/Describe Text Area
        Locator describeField = container.locator("textarea[name='describe'], textarea[name='your-message'], [data-name='your-message'] textarea").first();
        if (describeField.count() > 0) {
            safeFill(page, describeField, STATIC_DESCRIPTION);
        }
    }

    public static void handlePopupsIfPresent(Page page) {
        try {
            Locator onesignalAllow = page.locator("#onesignal-slidedown-allow-button");
            if (onesignalAllow.count() > 0) {
                try {
                    onesignalAllow.waitFor(new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(2000));
                    onesignalAllow.click();
                    onesignalAllow.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
                } catch (Exception e) {
                    // Ignored
                }
            }

            Locator consultationModal = page.locator("#rdemo_popup_modal, .rdemo_popup_modal, #rdemo_popup");
            if (consultationModal.count() > 0) {
                try {
                    consultationModal.waitFor(new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(2000));
                    Locator closeBtn = consultationModal.locator("button#close, button.btn-close, #close, .close-btn, button[data-dismiss='modal'], button[data-bs-dismiss='modal'], button.close, [aria-label='Close']").first();
                    closeBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
                    closeBtn.click();
                    consultationModal.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
                } catch (Exception e) {
                    // Ignored
                }
            }

            Locator genericModals = page.locator(".modal.show");
            int modalCount = genericModals.count();
            for (int i = 0; i < modalCount; i++) {
                Locator modal = genericModals.nth(i);
                String id = modal.getAttribute("id");
                if (id != null && !id.equals("quickContact")) {
                    Locator closeBtn = modal.locator(
                            "button.btn-close, button#close, #close, .close-btn, button[data-bs-dismiss='modal'], button.close, [aria-label='Close']")
                            .first();
                    if (closeBtn.isVisible()) {
                        closeBtn.click();
                        modal.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
                    }
                }
            }
        } catch (Exception e) {
            // Ignored
        }
    }

    public static String getCaptchaChallengeId(Locator container) {
        try {
            Locator challengeInput = container.locator("input[name^='_wpcf7_captcha_challenge_']").first();
            if (challengeInput.count() > 0) {
                return challengeInput.getAttribute("value");
            }
        } catch (Exception e) {
            // Ignored
        }
        return "";
    }

    public static String tryReadCaptchaFromDOM(Locator captchaImg) {
        try {
            String alt = captchaImg.getAttribute("alt");
            if (alt != null && !alt.equals("captcha") && alt.matches("^[a-zA-Z0-9]{4,6}$")) {
                return alt;
            }
        } catch (Exception e) {
            // Ignored
        }
        return null;
    }

    public static void preprocessImage(String inputPath, String outputPath, int threshold) {
        try {
            File inputFile = new File(inputPath);
            BufferedImage image = ImageIO.read(inputFile);

            int width = image.getWidth() * 3;
            int height = image.getHeight() * 3;
            BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = scaledImage.createGraphics();
            g.drawImage(image, 0, 0, width, height, null);
            g.dispose();

            BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = scaledImage.getRGB(x, y);
                    int r = (rgb >> 16) & 0xFF;
                    int gVal = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;
                    int gray = (int) (0.299 * r + 0.587 * gVal + 0.114 * b);

                    int binaryColor = (gray < threshold) ? 0x000000 : 0xFFFFFF;
                    grayImage.setRGB(x, y, (binaryColor == 0) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }

            File outputFile = new File(outputPath);
            ImageIO.write(grayImage, "png", outputFile);
        } catch (Exception e) {
            // Ignored
        }
    }

    public static String doTesseractOCR(String imagePath) throws TesseractException {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(TESSDATA_PATH);
        tesseract.setLanguage("eng");
        tesseract.setPageSegMode(8);
        tesseract.setVariable("tessedit_char_whitelist",
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789");
        String debugFile = System.getProperty("os.name").toLowerCase().contains("win") ? "NUL" : "/dev/null";
        tesseract.setVariable("debug_file", debugFile);

        File imgFile = new File(imagePath);
        return tesseract.doOCR(imgFile);
    }

    public static String cleanCaptchaText(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("[^a-zA-Z0-9]", "").trim();
    }

    public static boolean isValidCaptcha(String text) {
        return text != null && text.length() == 4;
    }

    public static void refreshCaptchaElement(Page page) {
        page.waitForTimeout(3000);
    }

    public static String generateRandomString(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    public static boolean navigateAndSubmit(Page page, String url, String containerSelector, String triggerSelector, String browserType) {
        // Register dynamic locator handler to close popup modals automatically whenever they appear
        try {
            Locator popupOverlay = page.locator("#rdemo_popup_modal, .rdemo_popup_modal, #rdemo_popup").first();
            page.addLocatorHandler(popupOverlay, (overlay) -> {
                System.out.println("[Framework] Dynamic popup detected. Closing it.");
                try {
                    Locator closeBtn = overlay.locator("button#close, button.btn-close, #close, .close-btn, button[data-dismiss='modal'], button[data-bs-dismiss='modal'], button.close, [aria-label='Close'], .close, span.close").first();
                    if (closeBtn.isVisible()) {
                        closeBtn.click();
                    }
                } catch (Exception e) {
                    // Ignore
                }
            });
        } catch (Exception e) {
            // Ignore if any registration errors
        }

        try {
            page.navigate(url, new Page.NavigateOptions().setWaitUntil(com.microsoft.playwright.options.WaitUntilState.COMMIT));
            checkAndDismissPopup(page);
        } catch (Exception e) {
            // Ignore transient navigation errors on early commit
        }

        long startTime = System.currentTimeMillis();
        long timeoutMs = 20000;
        Locator container = page.locator(containerSelector).first();
        Locator triggerBtn = triggerSelector != null ? page.locator(triggerSelector).first() : null;
        boolean clickedTrigger = false;

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            checkAndDismissPopup(page);
            handlePopupsIfPresent(page);

            if (container.count() > 0 && container.isVisible()) {
                break;
            }

            if (triggerBtn != null && !clickedTrigger && triggerBtn.count() > 0 && triggerBtn.isVisible()) {
                try {
                    safeClick(page, triggerBtn);
                    clickedTrigger = true;
                } catch (Exception e) {
                    // Ignore transient click errors
                }
            }

            page.waitForTimeout(100);
        }

        try {
            safeWaitFor(page, container, new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
        } catch (Exception e) {
            System.err.println("[" + browserType + "] Error: Target form " + containerSelector + " not visible.");
            return false;
        }

        return fillAndSubmitForm(page, containerSelector, browserType);
    }

    public static void checkAndDismissPopup(Page page) {
        if (!enablePopupWatcher || page == null) {
            return;
        }
        try {
            Locator popup = page.locator("#rdemo_popup_modal, .rdemo_popup_modal, #rdemo_popup, div.modal.show").first();
            if (popup.count() > 0 && popup.isVisible()) {
                String text = "";
                try {
                    text = popup.innerText();
                } catch (Exception e) {
                    // Ignore transient DOM detachment
                }
                if (text != null && text.contains("Get Free Consultation")) {
                    System.out.println("[Popup Handler] Popup detected.");
                    Locator closeBtn = popup.locator("button#close, button.btn-close, #close, .close-btn, button[data-dismiss='modal'], button[data-bs-dismiss='modal'], button.close, [aria-label='Close'], .close, span.close").first();
                    if (closeBtn.count() > 0 && closeBtn.isVisible()) {
                        System.out.println("[Popup Handler] Clicking close button.");
                        closeBtn.click();
                        try {
                            popup.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN).setTimeout(2000));
                            System.out.println("[Popup Handler] Popup closed successfully.");
                        } catch (Exception e) {
                            System.err.println("[Popup Handler] Warning: Popup did not close in time.");
                        }
                    } else {
                        // Fallback: search for child elements with class containing 'close' or 'Close'
                        Locator allChildren = popup.locator("*");
                        int count = allChildren.count();
                        boolean clicked = false;
                        for (int i = 0; i < count; i++) {
                            Locator child = allChildren.nth(i);
                            String cls = child.getAttribute("class");
                            if (cls != null && cls.toLowerCase().contains("close") && child.isVisible()) {
                                System.out.println("[Popup Handler] Clicking close button (fallback class): " + cls);
                                child.click();
                                clicked = true;
                                break;
                            }
                        }
                        if (clicked) {
                            try {
                                popup.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN).setTimeout(2000));
                                System.out.println("[Popup Handler] Popup closed successfully.");
                            } catch (Exception e) {
                                // Ignore
                            }
                        } else {
                            System.err.println("[Popup Handler] Close button not found or not visible.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[Popup Handler] Error occurred during popup check: " + e.getMessage());
        }
    }

    private static void safeFill(Page page, Locator locator, String value) {
        checkAndDismissPopup(page);
        locator.fill(value);
    }

    private static void safeClick(Page page, Locator locator) {
        checkAndDismissPopup(page);
        locator.click();
    }

    private static void safeSelectOption(Page page, Locator locator, String value) {
        checkAndDismissPopup(page);
        locator.selectOption(value);
    }

    private static void safeWaitFor(Page page, Locator locator, Locator.WaitForOptions options) {
        checkAndDismissPopup(page);
        locator.waitFor(options);
    }
}
