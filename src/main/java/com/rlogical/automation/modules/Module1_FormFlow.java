package com.rlogical.automation.modules;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;
import com.rlogical.automation.utils.AutomationHelper;

public class Module1_FormFlow {
    private static final String URL = "https://uat.rlogical.com/";

    public static boolean execute(Page page, String browserType) {
        return com.rlogical.automation.utils.FailureHandler.runWithFailureHandling(page, "Module1", browserType,
                (p) -> {
                    // 1. Navigate with COMMIT (returns immediately once HTML loading starts)
                    try {
                        p.navigate(URL, new Page.NavigateOptions().setWaitUntil(WaitUntilState.COMMIT));
                    } catch (Exception e) {
                        // Ignore early commit exceptions
                    }

                    // 2. Treat popup form as the highest priority form. Check if it appears.
                    // Poll for up to 5 seconds to see if the auto-popup modal shows up with the 'show' class
                    long startTime = System.currentTimeMillis();
                    Locator popup = p.locator("div.modal.show, #rdemo_popup_modal.show, .rdemo_popup_modal.show, #rdemo_popup.show").first();
                    boolean popupDetected = false;

                    while (System.currentTimeMillis() - startTime < 5000) {
                        if (popup.count() > 0 && popup.isVisible()) {
                            popupDetected = true;
                            break;
                        }
                        p.waitForTimeout(200);
                    }

                    if (popupDetected) {
                        System.out.println("[" + browserType + "] Consultation popup detected! Executing form submission directly on popup.");
                        String popupSelector = "div.modal.show";
                        if (p.locator("#rdemo_popup_modal.show").isVisible()) {
                            popupSelector = "#rdemo_popup_modal.show";
                        } else if (p.locator(".rdemo_popup_modal.show").isVisible()) {
                            popupSelector = ".rdemo_popup_modal.show";
                        } else if (p.locator("#rdemo_popup.show").isVisible()) {
                            popupSelector = "#rdemo_popup.show";
                        }
                        return AutomationHelper.fillAndSubmitForm(p, popupSelector, browserType);
                    }

                    // 3. Fallback: if popup is not present, use the existing homepage form detection logic (#quickContact)
                    System.out.println("[" + browserType + "] No consultation popup detected. Continuing with fallback homepage form (#quickContact).");
                    
                    // Close any other potential overlays
                    AutomationHelper.handlePopupsIfPresent(p);

                    // Wait/Click floating button to open #quickContact if not visible
                    Locator modal = p.locator("#quickContact");
                    Locator floatingBtn = p.locator("button.floating-contact-btn");

                    boolean modalVisible = false;
                    try {
                        modal.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(3000));
                        modalVisible = true;
                    } catch (Exception e) {
                        // Ignore
                    }

                    if (!modalVisible) {
                        try {
                            floatingBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
                            floatingBtn.click();
                        } catch (Exception e) {
                            // Ignore trigger errors
                        }
                    }

                    return AutomationHelper.fillAndSubmitForm(p, "#quickContact", browserType);
                });
    }
}
