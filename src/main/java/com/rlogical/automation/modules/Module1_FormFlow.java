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
                    p.navigate(URL, new Page.NavigateOptions().setWaitUntil(WaitUntilState.LOAD));

                    Locator modal = p.locator("#quickContact");
                    Locator floatingBtn = p.locator("button.floating-contact-btn");

                    boolean modalVisible = false;
                    try {
                        // Check if the form is already open / visible
                        modal.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(3000));
                        modalVisible = true;
                    } catch (Exception e) {
                        // Modal did not appear automatically
                    }

                    if (!modalVisible) {
                        // If not open, wait for the floating contact/mail button and click it to open the form
                        floatingBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(12000));
                        floatingBtn.click();
                    }

                    boolean form1Success = AutomationHelper.fillAndSubmitForm(p, "#quickContact", browserType);

                    p.waitForTimeout(3000);
                    return form1Success;
                });
    }
}
