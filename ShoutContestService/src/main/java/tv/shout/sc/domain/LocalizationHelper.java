package tv.shout.sc.domain;

import java.util.Map;

public class LocalizationHelper
{
    public static String getLocalizedString(Map<String, String> strings, String languageCode)
    {
        if (strings == null) return null;

        //if no language provided, use english
        if (languageCode == null) {
            languageCode = "en";
        }

        String val = strings.get(languageCode);

        if (val == null) {
            //if the given non-english language code didn't match anything, try english
            if (!languageCode.equals("en")) {
                val = strings.get("en");
            }
            return val;

        } else {
            return val;
        }
    }

}
