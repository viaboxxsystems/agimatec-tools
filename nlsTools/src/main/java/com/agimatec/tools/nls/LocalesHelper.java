package com.agimatec.tools.nls;

import com.agimatec.tools.nls.model.MBBundle;
import com.agimatec.tools.nls.model.MBBundles;
import com.agimatec.tools.nls.model.MBEntry;
import com.agimatec.tools.nls.model.MBText;

/**
 * Helper methods for bundle handling
 */
public class LocalesHelper {

    public static MBText findMBTextForLocale(String key, String locale, MBBundles bundles) {
        if (bundles != null) {
            for (MBBundle bundle : bundles.getBundles()) {
                for (MBEntry entry : bundle.getEntries()) {
                    if(entry.getKey().equals(key)) {
                        for(MBText text : entry.getTexts()) {
                            if(text.getLocale().equals(locale)) {
                                return text;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
