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
                    p.navigate(URL, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
                    p.waitForLoadState(LoadState.DOMCONTENTLOADED);
                    p.locator("body").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

                    // Wait up to 8 seconds for the OneSignal popup
                    Locator onesignalAllow = p.locator("#onesignal-slidedown-allow-button");
                    try {
                        onesignalAllow.waitFor(new Locator.WaitForOptions()
                                .setState(WaitForSelectorState.VISIBLE)
                                .setTimeout(8000));
                        onesignalAllow.click();
                        onesignalAllow.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
                    } catch (Exception e) {
                        // Ignore
                    }

                    AutomationHelper.handlePopupsIfPresent(p);

                    Locator modal = p.locator("#quickContact");
                    if (!modal.isVisible()) {
                        Locator floatingBtn = p.locator("button.floating-contact-btn");
                        if (floatingBtn.count() > 0 && floatingBtn.isVisible()) {
                            floatingBtn.click();
                        }
                    }
                    modal.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

                    boolean form1Success = AutomationHelper.fillAndSubmitForm(p, "#quickContact", browserType);

                    p.waitForTimeout(3000);
                    return form1Success;
                });
    }
}
