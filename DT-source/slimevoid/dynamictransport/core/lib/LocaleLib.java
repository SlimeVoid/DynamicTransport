package slimevoid.dynamictransport.core.lib;

import java.io.File;

import cpw.mods.fml.common.registry.LanguageRegistry;

public class LocaleLib {

    private static final String LANGUAGE_PATH = "/assets/dynamicelevator/locale";

    public static void registerLanguages() {
        File Dir = new File(LANGUAGE_PATH);
        // For every file specified in the localeFiles class, load them into the Language Registry
        if (Dir.list() != null) {
            for (String localizationFile : Dir.list()) {
                try{
                    LanguageRegistry.instance().loadLocalization(localizationFile,
                            getLocaleFromFileName(localizationFile),
                            isXMLLanguageFile(localizationFile));
                }finally{}
            }
        }
    }

    /***
     * Simple test to determine if a specified file name represents a XML file
     * or not
     * 
     * @param fileName
     *            String representing the file name of the file in question
     * @return True if the file name represents a XML file, false otherwise
     */
    public static boolean isXMLLanguageFile(String fileName) {
        return fileName.endsWith(".xml");
    }

    /***
     * Returns the locale from file name
     * 
     * @param fileName
     *            String representing the file name of the file in question
     * @return String representation of the locale snipped from the file name
     */
    public static String getLocaleFromFileName(String fileName) {
        return fileName.substring(fileName.lastIndexOf('/') + 1,
                fileName.lastIndexOf('.'));
    }

    public static String getLocalizedString(String key) {
        return LanguageRegistry.instance().getStringLocalization(key);
    }


}
