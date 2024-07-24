package com.infolog.googlesheets;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.infolog.util.ConfigLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the GoogleSheetsService class.
 */
@ExtendWith(MockitoExtension.class)
class GoogleSheetsServiceTest {

	private static final String TEST_SPREADSHEET_ID = "test_spreadsheet_id";
	private static final String TEST_CREDENTIALS_PATH = "src/test/resources/google-sheets-credentials.json";

	@Mock
	private Sheets mockSheets;
	@Mock
	private Sheets.Spreadsheets mockSpreadsheets;
	@Mock
	private Sheets.Spreadsheets.Values mockValues;
	@Mock
	private Sheets.Spreadsheets.Values.Update mockUpdate;

	private GoogleSheetsService googleSheetsService;

	/**
	 * Sets up the test environment before each test.
	 *
	 * @throws Exception if any error occurs during setup
	 */
	@BeforeEach
	void setUp() throws Exception {
		// Настройка мок-объектов и создание тестового экземпляра GoogleSheetsService
		setupMocks();
		createTestGoogleSheetsService();
		setupSheetsMocks();
	}

	/**
	 * Sets up mock objects for ConfigLoader to be used in testing.
	 */
	private void setupMocks() {
		try (MockedStatic<ConfigLoader> mockedConfigLoader = mockStatic(ConfigLoader.class)) {
			mockedConfigLoader.when(() -> ConfigLoader.getProperty("google.sheets.spreadsheet.id"))
					.thenReturn(TEST_SPREADSHEET_ID);
			mockedConfigLoader.when(() -> ConfigLoader.getProperty("google.sheets.credentials.file.path"))
					.thenReturn(TEST_CREDENTIALS_PATH);
		}
	}

	/**
	 * Creates a test instance of GoogleSheetsService and injects the mock Sheets
	 * object.
	 *
	 * @throws Exception if an error occurs during service creation or mock
	 *                   injection
	 */

	private void createTestGoogleSheetsService() throws Exception {
		googleSheetsService = new GoogleSheetsService();
		Field sheetsServiceField = GoogleSheetsService.class.getDeclaredField("sheetsService");
		sheetsServiceField.setAccessible(true);
		sheetsServiceField.set(googleSheetsService, mockSheets);
	}

	/**
	 * Sets up mock objects for Sheets to be used in testing.
	 *
	 * @throws IOException if an error occurs during mock setup
	 */
	private void setupSheetsMocks() throws IOException {
		when(mockSheets.spreadsheets()).thenReturn(mockSpreadsheets);
		when(mockSpreadsheets.values()).thenReturn(mockValues);
		when(mockValues.update(anyString(), anyString(), any())).thenReturn(mockUpdate);
		when(mockUpdate.setValueInputOption(anyString())).thenReturn(mockUpdate);
	}

	/**
	 * Tests the updateSheet method with sample data.
	 */
	@Test
	void testUpdateSheet() throws IOException {
		// Подготовка тестовых данных
		Map<String, Object> testData = prepareTestData();

		// Настройка мок-объекта для выполнения обновления
		when(mockUpdate.execute()).thenReturn(new UpdateValuesResponse());

		// Вызов тестируемого метода
		googleSheetsService.updateSheet(testData);

		// Проверка, что метод execute был вызван один раз
		verify(mockUpdate, times(1)).execute();
	}

	/**
	 * Tests that the updateSheet method formats data correctly.
	 */
	@Test
	void testUpdateSheetFormatsDataCorrectly() throws IOException {
		// Подготовка тестовых данных
		Map<String, Object> testData = prepareFormattedTestData();

		// Настройка мок-объекта для выполнения обновления
		when(mockUpdate.execute()).thenReturn(new UpdateValuesResponse());

		// Вызов тестируемого метода
		googleSheetsService.updateSheet(testData);

		// Проверка форматирования данных
		verify(mockValues).update(anyString(), anyString(), argThat(this::isDataFormattedCorrectly));
	}

	/**
	 * Tests that the updateSheet method handles IOException correctly.
	 */
	@Test
	void testUpdateSheetHandlesIOException() throws IOException {
		// Подготовка тестовых данных
		Map<String, Object> testData = new HashMap<>();
		testData.put("1", Map.of("49", 10, "969", 20));

		// Настройка мок-объекта для выброса исключения
		when(mockUpdate.execute()).thenThrow(new IOException("Test IO Exception"));

		// Проверка, что метод выбрасывает исключение
		assertThrows(IOException.class, () -> googleSheetsService.updateSheet(testData));
	}

	// Вспомогательные методы

	private Map<String, Object> prepareTestData() {
		Map<String, Object> testData = new HashMap<>();
		testData.put("1", Map.of("49", 10, "969", 20));
		testData.put("2", Map.of("49", 5, "969", 10));
		return testData;
	}

	private Map<String, Object> prepareFormattedTestData() {
		Map<String, Object> testData = new HashMap<>();
		testData.put("1", Map.of("49", 10, "969", 20));
		testData.put("2", Map.of("49", 5, "969", 10));
		testData.put("3", 100);
		testData.put("4", 50);
		return testData;
	}

	private boolean isDataFormattedCorrectly(ValueRange valueRange) {
		List<List<Object>> values = valueRange.getValues();
		return values.size() == 12 && values.get(0).size() == 5;
	}
}