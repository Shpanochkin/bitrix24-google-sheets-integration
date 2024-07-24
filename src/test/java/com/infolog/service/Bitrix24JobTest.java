package com.infolog.service;

import com.infolog.bitrix24.Bitrix24ApiClient;
import com.infolog.googlesheets.GoogleSheetsService;
import com.infolog.util.DateTimeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the Bitrix24Job class.
 */
@ExtendWith(MockitoExtension.class)
class Bitrix24JobTest {

	@Mock
	private Bitrix24ApiClient apiClient;

	@Mock
	private GoogleSheetsService sheetsService;

	@Mock
	private JobExecutionContext context;

	private Bitrix24Job bitrix24Job;

	/**
	 * Sets up the test environment before each test.
	 *
	 * @throws Exception if an error occurs during setup
	 */
	@BeforeEach
	void setUp() throws Exception {
		apiClient = mock(Bitrix24ApiClient.class);
		sheetsService = mock(GoogleSheetsService.class);
		bitrix24Job = new Bitrix24Job();
		context = mock(JobExecutionContext.class);

		injectMocks();
	}

	/**
	 * Injects mock objects into the Bitrix24Job instance using reflection.
	 *
	 * @throws Exception if an error occurs during mock injection
	 */
	private void injectMocks() throws Exception {
		injectMock("apiClient", apiClient);
		injectMock("sheetsService", sheetsService);
	}

	/**
	 * Injects a mock object into a specified field of the Bitrix24Job instance.
	 *
	 * @param fieldName  the name of the field to inject the mock into
	 * @param mockObject the mock object to inject
	 * @throws Exception if an error occurs during mock injection
	 */
	private void injectMock(String fieldName, Object mockObject) throws Exception {
		Field field = Bitrix24Job.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(bitrix24Job, mockObject);
	}

	/**
	 * Tests the normal execution of the Bitrix24Job.
	 *
	 * @throws Exception if an error occurs during test execution
	 */
	@Test
	void testExecute() throws Exception {
		when(apiClient.fetchBitrix24Data()).thenReturn("{\"key\": \"value\"}");

		bitrix24Job.execute(context);

		verify(apiClient, times(1)).fetchBitrix24Data();
		verify(sheetsService, times(1)).updateSheet(anyMap());
	}

	/**
	 * Tests that the job throws a JobExecutionException when the API client throws
	 * an exception.
	 *
	 * @throws Exception if an error occurs during test execution
	 */
	@Test
	void testExecuteWithApiClientException() throws Exception {
		when(apiClient.fetchBitrix24Data()).thenThrow(new RuntimeException("API Error"));

		assertThrows(JobExecutionException.class, () -> bitrix24Job.execute(context));
	}

	/**
	 * Tests that the job does not execute on a non-working day.
	 *
	 * @throws Exception if an error occurs during test execution
	 */
	@Test
	void testExecuteOnNonWorkingDay() throws Exception {
		LocalDate holiday = LocalDate.of(2023, 1, 1);
		try (MockedStatic<DateTimeUtils> mockedDateTimeUtils = mockStatic(DateTimeUtils.class);
				MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class)) {

			mockedDateTimeUtils.when(DateTimeUtils::getHolidays).thenReturn(Collections.singletonList(holiday));
			mockedLocalDate.when(LocalDate::now).thenReturn(holiday);

			bitrix24Job.execute(context);

			verify(apiClient, never()).fetchBitrix24Data();
			verify(sheetsService, never()).updateSheet(anyMap());
		}
	}

	/**
	 * Tests that the job handles exceptions thrown by the API client correctly.
	 *
	 * @throws Exception if an error occurs during test execution
	 */
	@Test
	void testExecuteHandlesApiClientException() throws Exception {
		when(apiClient.fetchBitrix24Data()).thenThrow(new RuntimeException("API Error"));

		assertThrows(JobExecutionException.class, () -> bitrix24Job.execute(context));
	}

	/**
	 * Tests that the job handles exceptions thrown by the Google Sheets service
	 * correctly.
	 *
	 * @throws Exception if an error occurs during test execution
	 */
	@Test
	void testExecuteHandlesGoogleSheetsServiceException() throws Exception {
		when(apiClient.fetchBitrix24Data()).thenReturn("{\"key\": \"value\"}");
		doThrow(new RuntimeException("Google Sheets Error")).when(sheetsService).updateSheet(anyMap());

		assertThrows(JobExecutionException.class, () -> bitrix24Job.execute(context));
	}
}