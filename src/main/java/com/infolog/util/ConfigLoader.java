package com.infolog.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * Utility class for loading and accessing configuration properties. This class
 * loads properties from a config.properties file in the classpath.
 */
public final class ConfigLoader {
	private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
	private static final Properties properties = new Properties();
	private static final String CONFIG_FILE = "config.properties";
	private static final String ERROR_LOADING_CONFIG = "Error loading configuration";
	private static final String CONFIG_NOT_FOUND = "config.properties not found in the classpath";

	static {
		loadConfig();
	}

	/**
	 * Loads the configuration from the config.properties file. This method is
	 * called in the static initializer block and can be used to reload the
	 * configuration.
	 *
	 * @throws RuntimeException if the config file is not found or cannot be loaded
	 */
	public static void loadConfig() {
		try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
			if (input == null) {
				logger.error("Unable to find {}", CONFIG_FILE);
				throw new RuntimeException(CONFIG_NOT_FOUND);
			}
			properties.load(input);
			logger.info("Configuration loaded successfully");
		} catch (IOException ex) {
			logger.error(ERROR_LOADING_CONFIG, ex);
			throw new RuntimeException(ERROR_LOADING_CONFIG, ex);
		}
	}

	/**
	 * Retrieves a property value for the given key.
	 *
	 * @param key the key of the property to retrieve
	 * @return the value of the property, or null if the key is not found
	 * @throws IllegalArgumentException if the key is null or empty
	 */
	public static String getProperty(String key) {
		validateKey(key);
		String value = properties.getProperty(key);
		if (value == null) {
			logger.warn("Property not found for key: {}", key);
		}
		return value;
	}

	/**
	 * Retrieves a property value for the given key, or returns the default value if
	 * the key is not found.
	 *
	 * @param key          the key of the property to retrieve
	 * @param defaultValue the default value to return if the key is not found
	 * @return the value of the property, or the default value if the key is not
	 *         found
	 * @throws IllegalArgumentException if the key is null or empty
	 */
	public static String getProperty(String key, String defaultValue) {
		validateKey(key);
		return properties.getProperty(key, defaultValue);
	}

	/**
	 * Validates that the given key is not null or empty.
	 *
	 * @param key the key to validate
	 * @throws IllegalArgumentException if the key is null or empty
	 */
	private static void validateKey(String key) {
		Objects.requireNonNull(key, "Property key cannot be null");
		if (key.trim().isEmpty()) {
			throw new IllegalArgumentException("Property key cannot be empty");
		}
	}

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 *
	 * @throws UnsupportedOperationException always, as this class should not be
	 *                                       instantiated
	 */
	private ConfigLoader() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}
}