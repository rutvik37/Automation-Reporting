package com.rlogical.automation.modules;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;
import com.rlogical.automation.utils.AutomationHelper;

public class Module3_CloudServiceCompanyFormFlow {
    private static final String URL = "https://uat.rlogical.com/cloud-service-company/";

    public static boolean execute(Page page, String browserType) {
        AutomationHelper.enablePopupWatcher = true;
        try {
            return com.rlogical.automation.utils.FailureHandler.runWithFailureHandling(page, "Module3", browserType,
                    (p) -> {
                        // Inject a client-side listener to automatically detect and close the "Get Free Consultation" popup modal
                        try {
                            p.addInitScript("(() => {\n" +
                                    "    function dismissPopup() {\n" +
                                    "        const selectors = ['#rdemo_popup_modal', '.rdemo_popup_modal', '#rdemo_popup', 'div.modal'];\n" +
                                    "        for (const selector of selectors) {\n" +
                                    "            const popup = document.querySelector(selector);\n" +
                                    "            if (popup) {\n" +
                                    "                const isVisible = popup.classList.contains('show') || \n" +
                                    "                                  popup.style.display === 'block' || \n" +
                                    "                                  (getComputedStyle(popup).display !== 'none' && getComputedStyle(popup).visibility !== 'hidden');\n" +
                                    "                if (isVisible) {\n" +
                                    "                    const isConsultation = popup.id === 'rdemo_popup_modal' || \n" +
                                    "                                           popup.classList.contains('rdemo_popup_modal') || \n" +
                                    "                                           popup.id === 'rdemo_popup' ||\n" +
                                    "                                           popup.innerText.includes('Get Free Consultation') ||\n" +
                                    "                                           popup.innerHTML.includes('Get Free Consultation');\n" +
                                    "                    if (isConsultation) {\n" +
                                    "                        // Find the Close button by class or standard selectors\n" +
                                    "                        let closeBtn = popup.querySelector('button#close, button.btn-close, #close, .close-btn, button[data-dismiss=\"modal\"], button[data-bs-dismiss=\"modal\"], button.close, [aria-label=\"Close\"], .close, span.close');\n" +
                                    "                        if (!closeBtn) {\n" +
                                    "                            // Fallback: search child elements for class containing 'close' or 'Close'\n" +
                                    "                            const children = popup.querySelectorAll('*');\n" +
                                    "                            for (const el of children) {\n" +
                                    "                                if (el.className && typeof el.className === 'string' && el.className.toLowerCase().includes('close')) {\n" +
                                    "                                    closeBtn = el;\n" +
                                    "                                    break;\n" +
                                    "                                }\n" +
                                    "                            }\n" +
                                    "                        }\n" +
                                    "                        if (closeBtn) {\n" +
                                    "                            closeBtn.click();\n" +
                                    "                            console.log('[JS-Observer] Closed consultation popup.');\n" +
                                    "                        }\n" +
                                    "                    }\n" +
                                    "                }\n" +
                                    "            }\n" +
                                    "        }\n" +
                                    "    }\n" +
                                    "    dismissPopup();\n" +
                                    "    const observer = new MutationObserver(() => dismissPopup());\n" +
                                    "    observer.observe(document.documentElement, { attributes: true, childList: true, subtree: true, attributeFilter: ['class', 'style'] });\n" +
                                    "    setInterval(dismissPopup, 200);\n" +
                                    "})();");
                        } catch (Exception e) {
                            System.err.println("[" + browserType + "] Error adding popup close init script: " + e.getMessage());
                        }

                        return AutomationHelper.navigateAndSubmit(p, URL, ".hire-dedicated-form", null, browserType);
                    });
        } finally {
            AutomationHelper.enablePopupWatcher = false;
        }
    }
}
