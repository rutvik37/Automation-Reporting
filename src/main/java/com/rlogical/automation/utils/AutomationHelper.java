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

    // Static form data values
    private static final String STATIC_NAME = "Tester Testing";
    private static final String STATIC_EMAIL = "testing5555@gmail.com";
    private static final String STATIC_MOBILE = "+91 1234567890";
    private static final String STATIC_COMPANY = "Testing company";
    private static final String STATIC_DESCRIPTION = "Testing Decription for verify these functionality";

    public static boolean fillAndSubmitForm(Page page, String containerSelector, String browserType) {
        Locator container = page.locator(containerSelector).first();

        try {
            container.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10000));
        } catch (Exception e) {
            System.err.println("[" + browserType + "] Error: Form container " + containerSelector + " not visible.");
            return false;
        }

        Locator captchaImg = container.locator("img.wpcf7-captchac, img[alt='captcha'], img[src*='captcha']").first();
        Locator captchaInput = container
                .locator("input.wpcf7-captchar, input[placeholder*='captcha'], input[name^='captcha-']").first();

        try {
            captchaImg.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(15000));
            captchaInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(15000));
        } catch (Exception e) {
            System.err.println("[" + browserType + "] Error: Captcha image or input element not found in form.");
            return false;
        }

        int maxSubmitAttempts = 3;
        String lastChallengeId = "";
        String lastImgSrc = "";

        try {
            for (int submitAttempt = 1; submitAttempt <= maxSubmitAttempts; submitAttempt++) {
                if (submitAttempt == 1) {
                    fillDummyDataForForm(page, container);
                } else {
                    // Wait for the captcha image src or challenge ID to change from lastImgSrc / lastChallengeId
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

                    // Refill all fields (name, email, number, company, description) on retry
                    fillDummyDataForForm(page, container);
                }

                Locator submitBtn = container.locator("input[type='submit']").first();
                try {
                    submitBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
                } catch (Exception e) {
                    System.err.println("[" + browserType + "] Error: Submit button not visible.");
                    return false;
                }

                boolean captchaSolved = solveCaptchaForForm(page, container, captchaImg, captchaInput, browserType);
                if (!captchaSolved) {
                    System.err.println("[" + browserType + "] Error: Captcha solving failed.");
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

                page.waitForTimeout(2000); // Wait 2 seconds before submitting form
                submitBtn.click();

                // Quick, responsive poll for success or validation error
                boolean isSuccess = false;
                boolean isComplete = false;
                String successMsg = "";
                String validationError = "";
                String captchaErrorText = "";

                long startTime = System.currentTimeMillis();
                long maxWaitMs = 20000; // max 20 seconds wait for AJAX/redirection response

                while (System.currentTimeMillis() - startTime < maxWaitMs) {
                    try {
                        String currentUrl = page.url();

                        // 1. Priority: URL change (redirection)
                        if (currentUrl.contains("/thank-you/") || currentUrl.contains("thank")) {
                            isSuccess = true;
                            isComplete = true;
                            successMsg = "Redirected to thank you page: " + currentUrl;
                            break;
                        }

                        // Find the form element to check native class states
                        Locator formEl = container.locator("form").count() > 0 ? container.locator("form").first() : container;
                        String formClass = (String) formEl.evaluate("el => el.className");

                        // 2. Priority: Contact Form 7 native class states
                        if (formClass != null) {
                            if (formClass.contains("sent") || formClass.contains("mail-sent-ok")) {
                                isSuccess = true;
                                isComplete = true;
                                successMsg = "Form class has sent state: " + formClass;
                                break;
                            }
                            if (formClass.contains("invalid") || formClass.contains("failed") || formClass.contains("spam")) {
                                isSuccess = false;
                                isComplete = true;
                                validationError = "Form class has error state: " + formClass;
                                break;
                            }
                        }

                        // 3. Priority: Response output element text
                        Locator responseOutput = container.locator("div.wpcf7-response-output").first();
                        if (responseOutput.count() > 0 && responseOutput.isVisible()) {
                            String responseText = responseOutput.innerText().trim();
                            if (!responseText.isEmpty()) {
                                successMsg = "Response output text: " + responseText;
                                // Check if it is a success message
                                if (responseText.toLowerCase().contains("thank you")
                                        || responseText.toLowerCase().contains("sent")
                                        || responseText.toLowerCase().contains("success")
                                        || responseText.contains("Thank You for Contacting Us")) {
                                    isSuccess = true;
                                } else {
                                    isSuccess = false;
                                    validationError = "Response output indicates failure: " + responseText;
                                }
                                isComplete = true;
                                break;
                            }
                        }

                        // 4. Priority: Check for validation tip messages (e.g. captcha error or empty fields)
                        Locator validationTips = container.locator(".wpcf7-not-valid-tip");
                        if (validationTips.count() > 0) {
                            StringBuilder errors = new StringBuilder();
                            for (int i = 0; i < validationTips.count(); i++) {
                                if (validationTips.nth(i).isVisible()) {
                                    String errText = validationTips.nth(i).innerText().trim();
                                    errors.append("[").append(errText).append("] ");
                                    if (errText.toLowerCase().contains("captcha") || errText.toLowerCase().contains("code")) {
                                        captchaErrorText = errText;
                                    }
                                }
                            }
                            if (errors.length() > 0) {
                                isSuccess = false;
                                isComplete = true;
                                validationError = "Validation tips visible: " + errors.toString();
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
                    System.out.println("[" + browserType + "] Success detected! Message: " + successMsg);
                } else {
                    System.err.println("[" + browserType + "] Failure detected!");
                    if (!validationError.isEmpty()) {
                        System.err.println("[" + browserType + "] Validation Error: " + validationError);
                    }
                    if (!captchaErrorText.isEmpty()) {
                        System.err.println("[" + browserType + "] Captcha Error: " + captchaErrorText);
                    }
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

                if (!isComplete) {
                    System.err.println("[" + browserType + "] Attempt " + submitAttempt + " timed out waiting for redirection.");
                } else {
                    System.err.println("[" + browserType + "] Attempt " + submitAttempt + " failed to redirect/show success message.");
                }
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
                captchaImg.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
                captchaInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            } catch (Exception e) {
                return false;
            }

            String initialChallenge = getCaptchaChallengeId(container);
            String extractedCode = tryReadCaptchaFromDOM(captchaImg);

            if (extractedCode == null || extractedCode.trim().isEmpty()) {
                try {
                    Files.createDirectories(Paths.get("scratch"));
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

            captchaInput.fill(extractedCode);
            page.waitForTimeout(1500); // Wait 1.5 seconds after filling captcha

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
        Locator companyField = container.locator("input[name='company']").first();
        companyField.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        companyField.fill(STATIC_COMPANY);

        Locator fnameField = container.locator("input[name='fname']").first();
        fnameField.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        fnameField.fill(STATIC_NAME);

        Locator emailField = container.locator("input[name='email']").first();
        emailField.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        emailField.fill(STATIC_EMAIL);

        Locator mobileField = container.locator("input[name='mobile']").first();
        mobileField.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        String cleanedMobile = STATIC_MOBILE.replaceAll("[^0-9]", "");
        if (cleanedMobile.length() > 10) {
            cleanedMobile = cleanedMobile.substring(cleanedMobile.length() - 10);
        }
        mobileField.fill(cleanedMobile);

        Locator categoryField = container.locator("select[name='category']").first();
        if (categoryField.count() > 0 && categoryField.isVisible()) {
            categoryField.selectOption("Hire Developers");
            try {
                categoryField.evaluate("el => el.dispatchEvent(new Event('change', { bubbles: true }))");
            } catch (Exception e) {
                // Ignore
            }
        }

        Locator describeField = container.locator("textarea[name='describe']").first();
        describeField.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        describeField.fill(STATIC_DESCRIPTION);
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

            Locator consultationModal = page.locator("#rdemo_popup_modal");
            if (consultationModal.count() > 0) {
                try {
                    consultationModal.waitFor(new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(2000));
                    Locator closeBtn = consultationModal.locator("button#close, button[data-dismiss='modal']").first();
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
                            "button.btn-close, button[data-bs-dismiss='modal'], button[data-dismiss='modal'], #close")
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
}
