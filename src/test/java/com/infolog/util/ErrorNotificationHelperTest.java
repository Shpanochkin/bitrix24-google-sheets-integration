package com.infolog.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * Unit tests for the ErrorNotificationHelper class.
 */
@ExtendWith(MockitoExtension.class)
class ErrorNotificationHelperTest {

	/**
	 * Tests that notifyAdminAboutError method calls
	 * TelegramNotificationService.sendNotification.
	 */
	@Test
	void testNotifyAdminAboutError() {
		try (MockedStatic<TelegramNotificationService> mockedStatic = mockStatic(TelegramNotificationService.class)) {
			ErrorNotificationHelper.notifyAdminAboutError("TestClass", "testMethod", "Test error",
					new RuntimeException("Test exception"));
			mockedStatic.verify(() -> TelegramNotificationService.sendNotification(anyString()), times(1));
		}
	}

	/**
	 * Tests that notifyAdminAboutSuccess method calls
	 * TelegramNotificationService.sendNotification.
	 */
	@Test
	void testNotifyAdminAboutSuccess() {
		try (MockedStatic<TelegramNotificationService> mockedStatic = mockStatic(TelegramNotificationService.class)) {
			ErrorNotificationHelper.notifyAdminAboutSuccess("TestJob");
			mockedStatic.verify(() -> TelegramNotificationService.sendNotification(anyString()), times(1));
		}
	}

	/**
	 * Tests that the error message format in notifyAdminAboutError method is
	 * correct.
	 */
	@Test
	void testErrorMessageFormat() {
		try (MockedStatic<TelegramNotificationService> mockedStatic = mockStatic(TelegramNotificationService.class)) {
			Exception testException = new RuntimeException("Test exception");
			ErrorNotificationHelper.notifyAdminAboutError("TestClass", "testMethod", "Test error message",
					testException);
			mockedStatic.verify(
					() -> TelegramNotificationService.sendNotification(
							argThat(message -> message.contains("TestClass") && message.contains("testMethod")
									&& message.contains("Test error message") && message.contains("Test exception"))),
					times(1));
		}
	}
}