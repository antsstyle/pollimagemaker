/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.pollimagemaker.configuration;

import antsstyle.pollimagemaker.db.CoreDB;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.TreeMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Ant
 */
public class DefaultConfig {

    /**
     * The properties object used to store the various properties and their values.
     */
    protected static final Properties properties = new Properties();
    private static final Logger LOGGER = LogManager.getLogger();

    private static boolean defaultValuesInitialised = false;

    /**
     * Map of property name strings in the artposter.properties file to fields in this class.
     */
    protected static final TreeMap<String, Field> configFields = new TreeMap<>();
    // Map of property name strings to the default values listed here

    /**
     * Map storing the different properties by name, along with their default values.
     */
    protected static final TreeMap<String, Object> defaultValues = new TreeMap<>();

    protected static final String S_AUTO_CROP = "pollimagemaker.autocrop";
    protected static final String S_RECT_ASPECTR_HIGHER_LIM = "pollimagemaker.rectaspectrhigherlim";
    protected static final String S_RECT_ASPECTR_LOWER_LIM = "pollimagemaker.rectaspectrlowerlim";
    protected static final String S_SQUARE_ASPECTR_HIGHER_LIM = "pollimagemaker.squareaspectrhigherlim";
    protected static final String S_SQUARE_ASPECTR_LOWER_LIM = "pollimagemaker.squareaspectrlowerlim";
    protected static final String S_SAVE_CROPPED_IMAGES = "pollimagemaker.savecroppedimages";
    protected static final String S_BORDER_THICKNESS = "pollimagemaker.borderthickness";
    protected static final String S_CROP_BUTTONS_HEIGHT = "pollimagemaker.cropbuttonsheight";
    protected static final String S_CROP_BUTTONS_WIDTH = "pollimagemaker.cropbuttonswidth";
    protected static final String S_OMIT_NUMBERS = "pollimagemaker.omitnumbers";
    protected static final String S_SAVE_DIRECTORY = "pollimagemaker.savedirectory";

    public static String SAVE_DIRECTORY = "";
    public static boolean AUTO_CROP = false;
    public static boolean SAVE_CROPPED_IMAGES = false;
    public static boolean OMIT_NUMBERS = false;
    public static double RECT_ASPECTR_HIGHER_LIM = 2.20;
    public static double RECT_ASPECTR_LOWER_LIM = 1.80;
    public static double SQUARE_ASPECTR_HIGHER_LIM = 1.10;
    public static double SQUARE_ASPECTR_LOWER_LIM = 0.90;
    public static int BORDER_THICKNESS = 1;
    public static int CROP_BUTTONS_HEIGHT = 25;
    public static int CROP_BUTTONS_WIDTH = 125;

    public static void saveConfiguration() {
        LOGGER.debug("Saving configuration...");
        Field[] fields = DefaultConfig.class.getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            if (!fieldName.startsWith("S_")) {
                try {
                    String nameFieldValue = String.valueOf(DefaultConfig.class.getDeclaredField("S_".concat(fieldName))
                            .get(null));
                    Object fieldValue = field.get(null);
                    int res;
                    if (fieldValue != null) {
                        res = CoreDB.insertSetting(nameFieldValue, String.valueOf(fieldValue));
                        if (res < 0) {
                            LOGGER.warn("Error saving configuration value for field: " + nameFieldValue);
                        }
                    }

                } catch (NoSuchFieldException nsfe) {
                } catch (Exception e) {
                    LOGGER.error(e);
                }
            }
        }
    }

    /**
     * Initialises the properties map with the default values for each field.
     */
    public static void initialisePropertiesMap() {
        if (defaultValuesInitialised) {
            return;
        }
        Field[] fields = DefaultConfig.class.getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            if (!fieldName.startsWith("S_")) {
                try {
                    String nameFieldValue = String.valueOf(DefaultConfig.class.getDeclaredField("S_".concat(fieldName))
                            .get(null));
                    Object fieldValue = field.get(null);
                    defaultValues.put(nameFieldValue, fieldValue);
                } catch (NoSuchFieldException nsfe) {
                } catch (Exception e) {
                    LOGGER.error(e);
                }
            } else {
                try {
                    Field valueField = DefaultConfig.class.getDeclaredField(fieldName.substring(2, fieldName.length()));
                    configFields.put((String) field.get(null), valueField);
                } catch (Exception e) {
                    LOGGER.error(e);
                }
            }
        }
        defaultValuesInitialised = true;
    }
}
