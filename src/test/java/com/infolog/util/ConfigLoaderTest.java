package com.infolog.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ConfigLoader class.
 */
class ConfigLoaderTest {

	/**
	 * Ensures the configuration is loaded before each test.
	 */
	@BeforeEach
	void setUp() {
		ConfigLoader.loadConfig();
	}

	/**
	 * Tests that getProperty returns a non-null and non-empty value for an existing
	 * property.
	 */
	@Test
	void testGetProperty() {
		String value = ConfigLoader.getProperty("app.name");
		assertNotNull(value);
		assertFalse(value.isEmpty());
	}

	/**
	 * Tests that getProperty returns the default value for a non-existent property.
	 */
	@Test
	void testGetPropertyWithDefault() {
		String value = ConfigLoader.getProperty("non.existent.property", "default");
		assertEquals("default", value);
	}

	/**
	 * Tests that getProperty throws a NullPointerException when given a null key.
	 */
	@Test
	void testGetPropertyThrowsExceptionForNullKey() {
		assertThrows(NullPointerException.class, () -> ConfigLoader.getProperty(null));
	}

	/**
	 * Tests that getProperty throws an IllegalArgumentException when given an empty
	 * key.
	 */
	@Test
	void testGetPropertyThrowsExceptionForEmptyKey() {
		assertThrows(IllegalArgumentException.class, () -> ConfigLoader.getProperty(""));
	}
}