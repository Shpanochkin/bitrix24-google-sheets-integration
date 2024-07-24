package com.infolog.googlesheets;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.infolog.util.ConfigLoader;
import com.infolog.util.ErrorNotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service class for interacting with Google Sheets API. This class is
 * responsible for updating a specific Google Sheets spreadsheet with data from
 * Bitrix24.
 */
public class GoogleSheetsService {
	private static final Logger logger = LoggerFactory.getLogger(GoogleSheetsService.class);
	private static final String SHEET_NAME = "Для бота2";
	private static final String RANGE = "A10:E21";
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	private static final String APPLICATION_NAME = "Bitrix24 Google Sheets Integration";
	private static final String DATE_FORMAT = "dd.MM.yyyy";
	private static final String SPREADSHEET_ID_PROPERTY = "google.sheets.spreadsheet.id";
	private static final String CREDENTIALS_FILE_PATH_PROPERTY = "google.sheets.credentials.file.path";
	private static final String VALUE_INPUT_OPTION = "USER_ENTERED";
	private static final String[] MANAGER_IDS = { "49", "969", "2879", "16998", "14829" };
	private static final String[] MANAGER_NAMES = { "Степанов Александр", "Кондратьев Олег", "Латыпова Марина",
			"Алексей Канатников", "Мария Бескаравайная" };
	private final Sheets sheetsService;
	private final String spreadsheetId;

	/**
	 * Constructs a new GoogleSheetsService. Initializes the Google Sheets service
	 * and spreadsheet ID.
	 *
	 * @throws IOException              if there's an error reading the credentials
	 *                                  file
	 * @throws GeneralSecurityException if there's a security-related error
	 * @throws IllegalStateException    if required configuration properties are
	 *                                  missing
	 */
	public GoogleSheetsService() throws IOException, GeneralSecurityException {
		this.spreadsheetId = Objects.requireNonNull(ConfigLoader.getProperty(SPREADSHEET_ID_PROPERTY),
				"Spreadsheet ID is not configured properly");
		this.sheetsService = createSheetsService();
		logger.info("GoogleSheetsService initialized with spreadsheet ID: {}", this.spreadsheetId);
	}

	/**
	 * Creates and configures the Google Sheets service.
	 *
	 * @return Configured Sheets service
	 * @throws IOException              if there's an error reading the credentials
	 *                                  file
	 * @throws GeneralSecurityException if there's a security-related error
	 * @throws IllegalStateException    if the credentials file path is not
	 *                                  configured properly
	 */
	private Sheets createSheetsService() throws IOException, GeneralSecurityException {
		String credentialsFilePath = Objects.requireNonNull(ConfigLoader.getProperty(CREDENTIALS_FILE_PATH_PROPERTY),
				"Credentials file path is not configured properly");

		try (FileInputStream credentialsStream = new FileInputStream(credentialsFilePath)) {
			GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
					.createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

			return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY,
					new HttpCredentialsAdapter(credentials)).setApplicationName(APPLICATION_NAME).build();
		}
	}

	/**
	 * Updates the Google Sheet with the provided data.
	 *
	 * @param data Map containing the data to be updated in the sheet
	 * @throws IOException if there's an error updating the sheet
	 */
	public void updateSheet(Map<String, Object> data) throws IOException {
		List<List<Object>> values = prepareData(data);
		ValueRange body = new ValueRange().setValues(values);

		logger.info("Updating Google Sheets with data: {}", values);

		try {
			UpdateValuesResponse result = sheetsService.spreadsheets().values()
					.update(spreadsheetId, SHEET_NAME + "!" + RANGE, body).setValueInputOption(VALUE_INPUT_OPTION)
					.execute();

			logger.info("Google Sheets update: {} cells updated.", result.getUpdatedCells());
		} catch (IOException e) {
			logger.error("Error updating Google Sheets", e);
			ErrorNotificationHelper.notifyAdminAboutError("GoogleSheetsService", "updateSheet",
					"Error updating Google Sheets", e);
			throw e;
		}
	}

	/**
	 * Prepares data for updating the Google Sheet.
	 *
	 * @param data Map containing the data to be inserted into the sheet
	 * @return List of lists representing the rows and columns of data
	 */
    private List<List<Object>> prepareData(Map<String, Object> data) {
        List<List<Object>> values = new ArrayList<>();
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT));

        // Add manager rows (A10:E14)
        for (int i = 0; i < MANAGER_IDS.length; i++) {
            String managerId = MANAGER_IDS[i];
            List<Object> row = new ArrayList<>();
            row.add(i == 0 ? currentDate : "");
            row.add(MANAGER_NAMES[i]);
            row.add(getValueOrDefault(data, "1", managerId, "Фильтр 1 (айди " + managerId + ")"));
            row.add(getValueOrDefault(data, "2", managerId, "Фильтр 2 (айди " + managerId + ")"));
            row.add(String.format("=D%d/C%d", i + 10, i + 10)); // E10:E14 formulas
            values.add(row);
        }

        // Add summary rows (A15:E21)
        String[][] summaryData = {
            {"АВ с рекламы", "3", "4"},
            {"Всего с рекламы за 30", "5", "6"},
            {"Остальной трафик", "=C18-C16", "=D18-D16"},
            {"Всего АВС за 30д", "7", "8"},
            {"Всего АВ за 30д", "11", "12"},
            {"АБ с рекламы сегодня", "9", "13"},
            {"АБ с рекламы за тек.м", "10", "14"}
        };

        for (int i = 0; i < summaryData.length; i++) {
            List<Object> row = new ArrayList<>();
            row.add("");
            row.add(summaryData[i][0]);
            
            String cValue = summaryData[i][1].startsWith("=") ? summaryData[i][1] : 
                            safeToString(getValueOrDefault(data, summaryData[i][1], null, "Фильтр " + summaryData[i][1]));
            String dValue = summaryData[i][2].startsWith("=") ? summaryData[i][2] : 
                            safeToString(getValueOrDefault(data, summaryData[i][2], null, "Фильтр " + summaryData[i][2]));
            
            row.add(cValue);
            row.add(dValue);
            
            // Add conversion formula for all rows except "Остальной трафик"
            if (i != 2) {
                row.add(String.format("=D%d/C%d", i + 15, i + 15));
            } else {
                row.add("=D17/C17");
            }
            
            values.add(row);
        }

        logger.info("Prepared data: {}", values);
        return values;
    }

	/**
	 * Gets a value from the data map or returns a default value.
	 * If subKey is null, it retrieves the value directly from the top-level map.
	 *
	 * @param data The data map
	 * @param key The key to look up
	 * @param subKey The inner key (can be null for top-level values)
	 * @param defaultValue The default value to return if not found
	 * @return The value if found, or the default value if not found
	 */
    private Object getValueOrDefault(Map<String, Object> data, String key, String subKey, String defaultValue) {
        if (subKey == null) {
            return data.getOrDefault(key, defaultValue);
        }
        
        if (data.containsKey(key)) {
            Object value = data.get(key);
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> subMap = (Map<String, Object>) value;
                return subMap.getOrDefault(subKey, defaultValue);
            }
        }
        return defaultValue;
    }

    private String safeToString(Object obj) {
        return obj == null ? "" : obj.toString();
    }
}