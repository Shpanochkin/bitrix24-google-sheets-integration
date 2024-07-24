package com.infolog.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the TelegramNotificationService class.
 */
@ExtendWith(MockitoExtension.class)
class TelegramNotificationServiceTest {

	/**
	 * Tests that sendNotification method doesn't throw an exception when called
	 * with a valid message.
	 */
	@Test
	void testSendNotification() {
		try (MockedStatic<ConfigLoader> mockedConfigLoader = mockStatic(ConfigLoader.class)) {
			mockedConfigLoader.when(() -> ConfigLoader.getProperty("telegram.bot.token")).thenReturn("test_token");
			assertDoesNotThrow(() -> TelegramNotificationService.sendNotification("Test message"));
		}
	}

	/**
	 * Tests that sendNotification method throws a NullPointerException when called
	 * with a null message.
	 */
	@Test
	void testSendNotificationWithNullMessage() {
		assertThrows(NullPointerException.class, () -> TelegramNotificationService.sendNotification(null));
	}

	/**
	 * Tests that sendNotification method throws an IllegalArgumentException when
	 * called with an empty message.
	 */
	@Test
	void testSendNotificationWithEmptyMessage() {
		assertThrows(IllegalArgumentException.class, () -> TelegramNotificationService.sendNotification(""));
	}
}