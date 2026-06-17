package com.rlogical.automation.modules;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;
import com.rlogical.automation.utils.AutomationHelper;

public class Module2_ContactFormFlow {
    private static final String URL = "https://uat.rlogical.com/";

    public static boolean execute(Page page, String browserType) {
        return com.rlogical.automation.utils.FailureHandler.runWithFailureHandling(page, "Module2", browserType,
                (p) -> {
                    p.navigate(URL, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

                    // Close the quickContact popup modal if it appears and blocks the page
                    Locator quickContactModal = p.locator("#quickContact");
                    try {
                        quickContactModal.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(3000));
                        Locator closeBtn = quickContactModal.locator("button.btn-close, button[data-bs-dismiss='modal'], button[data-dismiss='modal'], #close").first();
                        if (closeBtn.count() > 0 && closeBtn.isVisible()) {
                            closeBtn.click();
                            quickContactModal.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN).setTimeout(5000));
                        }
                    } catch (Exception e) {
                        // Ignore if popup does not appear
                    }

                    // Click on the "Get in touch" button (checks for both desktop menu item and mobile contact link)
                    Locator getInTouchBtn = p.locator("li.touch_btn a, a.mobile-contact-btn").first();
                    getInTouchBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(15000));
                    getInTouchBtn.click();

                    // Wait for the contact page form container to be visible
                    Locator formContainer = p.locator("form.wpcf7-form").first();
                    formContainer.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(15000));

                    // Fill and submit the form
                    boolean formSuccess = AutomationHelper.fillAndSubmitForm(p, "form.wpcf7-form", browserType);

                    p.waitForTimeout(3000);
                    return formSuccess;
                });
    }
}
