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
                (p) -> AutomationHelper.navigateAndSubmit(p, URL, "form.wpcf7-form:has(select[name='category'])", "li.touch_btn a, a.mobile-contact-btn", browserType));
    }
}
