/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.pollimagemaker.configuration;

import antsstyle.pollimagemaker.db.CoreDB;
import java.lang.reflect.Field;
import java.util.Set;
import java.util.TreeMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Holds configuration variables and associated values. See DefaultConfig for information about variables this class holds.
 *
 * @author Ant
 * @see DefaultConfig
 * @since 1.0
 */
public final class Config extends DefaultConfig {

    private static final Logger LOGGER = LogManager.getLogger();

    private Config() {

    }

    /**
     * Initialises the Config class and loads the properties map.
     *
     * @return True if configuration was initialised successfully; false otherwise.
     */
    public static boolean initialise() {
        DefaultConfig.initialisePropertiesMap();
        LOGGER.debug("Loading configuration...");
        TreeMap<String, String> allSettings = CoreDB.getAllSettings();
        try {
            for (String key : allSettings.keySet()) {
                properties.setProperty(key, allSettings.get(key));
            }
        } catch (Exception e) {
            LOGGER.error("Error loading configuration settings from database!", e);
            return false;
        }

        Set<String> propertyKeys = properties.stringPropertyNames();
        for (String key : propertyKeys) {
            String propertyValue = properties.getProperty(key);
            if (propertyValue.trim()
                    .equals("")) {
                continue;
            }
            Field field = configFields.get(key);
            Object valueToSet = null;
            if (propertyValue.toLowerCase()
                    .equals("true") || propertyValue.toLowerCase()
                    .equals("false")) {
                valueToSet = Boolean.valueOf(propertyValue);
            } else {
                try {
                    Integer integer = Integer.parseInt(propertyValue);
                    valueToSet = integer;
                } catch (NumberFormatException nfe) {
                    try {
                        Double d = Double.parseDouble(propertyValue);
                        valueToSet = d;
                    } catch (NumberFormatException n) {
                        
                    }
                }

                if (valueToSet == null) {
                    valueToSet = propertyValue;
                }

            }
            try {
                if (!valueToSet.equals("")) {
                    field.set(null, valueToSet);
                }
            } catch (Exception e) {
                if (e instanceof NullPointerException) {
                    LOGGER.error("Unknown configuration value in database: " + key + "     " + propertyValue);
                } else {
                    LOGGER.error(e);
                }
            }
        }
        return true;
    }

}
