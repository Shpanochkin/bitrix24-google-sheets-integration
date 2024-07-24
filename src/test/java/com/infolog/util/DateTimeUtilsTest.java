package com.infolog.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the DateTimeUtils class.
 */
@ExtendWith(MockitoExtension.class)
class DateTimeUtilsTest {

	/**
	 * Tests that getHolidays returns a non-null and non-empty list of holidays.
	 */
	@Test
	void testGetHolidays() {
		List<LocalDate> holidays = DateTimeUtils.getHolidays();
		assertNotNull(holidays);
		assertFalse(holidays.isEmpty());
	}

	/**
	 * Tests that fetchHolidaysFromApi returns the expected list of holidays.
	 */
	@Test
	void testFetchHolidaysFromApi() {
		try (MockedStatic<DateTimeUtils> mockedDateTimeUtils = mockStatic(DateTimeUtils.class)) {
			int testYear = LocalDate.now().getYear();
			List<LocalDate> testHolidays = List.of(LocalDate.of(testYear, 1, 1), LocalDate.of(testYear, 1, 7),
					LocalDate.of(testYear, 5, 9));
			mockedDateTimeUtils.when(() -> DateTimeUtils.fetchHolidaysFromApi(testYear)).thenReturn(testHolidays);

			List<LocalDate> result = DateTimeUtils.fetchHolidaysFromApi(testYear);
			assertEquals(testHolidays, result);
		}
	}

	/**
	 * Tests that getDefaultHolidays returns a non-null and non-empty list of
	 * default holidays.
	 */
	@Test
	void testGetDefaultHolidays() {
		int testYear = 2023;
		List<LocalDate> defaultHolidays = DateTimeUtils.getDefaultHolidays(testYear);
		assertNotNull(defaultHolidays);
		assertFalse(defaultHolidays.isEmpty());
		assertTrue(defaultHolidays.contains(LocalDate.of(testYear, 1, 1)));
		assertTrue(defaultHolidays.contains(LocalDate.of(testYear, 5, 9)));
	}

	/**
	 * Tests that the holiday cache is updated correctly and returns consistent
	 * results.
	 */
	@Test
	void testUpdateHolidayCache() {
		int currentYear = LocalDate.now().getYear();

		// First call should fetch holidays
		List<LocalDate> firstCallHolidays = DateTimeUtils.getHolidays();
		assertNotNull(firstCallHolidays);
		assertTrue(firstCallHolidays.stream().allMatch(date -> date.getYear() == currentYear));

		// Second call should return cached holidays
		List<LocalDate> secondCallHolidays = DateTimeUtils.getHolidays();
		assertNotNull(secondCallHolidays);
		assertEquals(firstCallHolidays, secondCallHolidays);

		// Verify that all holidays are for the current year
		assertTrue(secondCallHolidays.stream().allMatch(date -> date.getYear() == currentYear));
	}

	/**
	 * Tests that fetchHolidaysFromApi throws IllegalArgumentException for invalid
	 * years.
	 */
	@Test
	void testFetchHolidaysFromApiWithInvalidYear() {
		assertThrows(IllegalArgumentException.class, () -> DateTimeUtils.fetchHolidaysFromApi(1800));
		assertThrows(IllegalArgumentException.class, () -> DateTimeUtils.fetchHolidaysFromApi(2200));
	}
}