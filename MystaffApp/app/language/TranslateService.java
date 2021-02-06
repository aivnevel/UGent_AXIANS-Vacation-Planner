package language;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class TranslateService {

    private static final String FILENAME = "resources/text";

    private static TranslateService _instance;
    private static ResourceBundle bundle;

    private TranslateService() {}

    public static TranslateService getInstance() {
        if (_instance == null) {
            _instance = new TranslateService();
        }
        return _instance;
    }

    public void loadBundle(Locale locale) {
        try {
            bundle = ResourceBundle.getBundle(FILENAME, locale);
        } catch (MissingResourceException ex) {
            // Use English in case 'locale' isn't supported
            bundle = ResourceBundle.getBundle(FILENAME, Locale.ENGLISH);
        }
    }

    public String getWord(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException ex) {
            return key;
        }
    }
}
