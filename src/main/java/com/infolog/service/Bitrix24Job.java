package com.infolog.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infolog.bitrix24.Bitrix24ApiClient;
import com.infolog.googlesheets.GoogleSheetsService;
import com.infolog.util.DateTimeUtils;
import com.infolog.util.ErrorNotificationHelper;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

/**
 * Job class for fetching data from Bitrix24 and updating Google Sheets. This
 * job is scheduled to run periodically to sync data between Bitrix24 and Google
 * Sheets.
 */
public class Bitrix24Job implements Job {
	private static final Logger logger = LoggerFactory.getLogger(Bitrix24Job.class);
	private static final String ERROR_EXECUTING_JOB = "Error executing Bitrix24 job";
	private static final String CLASS_NAME = "Bitrix24Job";
	private static final String METHOD_NAME = "execute";

	private final Bitrix24ApiClient apiClient;
	private final ObjectMapper objectMapper;
	private final GoogleSheetsService sheetsService;

	/**
	 * Constructs a new Bitrix24Job. Initializes the API client, object mapper, and
	 * Google Sheets service.
	 *
	 * @throws Exception if there's an error initializing the job components
	 */
	public Bitrix24Job() throws Exception {
		try {
			this.apiClient = new Bitrix24ApiClient();
			this.objectMapper = new ObjectMapper();
			this.sheetsService = new GoogleSheetsService();
		} catch (Exception e) {
			logAndNotifyError("Error initializing Bitrix24Job", e);
			throw e;
		}
	}

	/**
	 * Executes the job to fetch data from Bitrix24 and update Google Sheets.
	 *
	 * @param context the JobExecutionContext
	 * @throws JobExecutionException if there's an error executing the job
	 */
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
	    Objects.requireNonNull(context, "JobExecutionContext cannot be null");
		logger.info("Executing Bitrix24 job");

		if (!isWorkingDay()) {
			logger.info("Today is not a working day. Skipping job execution.");
			return;
		}

		try {
			Map<String, Object> data = fetchAndParseData();
			updateGoogleSheets(data);
			notifySuccess();
		} catch (Exception e) {
			handleJobExecutionError(e);
		}
	}

	/**
	 * Checks if the current day is a working day.
	 * A working day is defined as a weekday (Monday to Friday) that is not a holiday.
	 *
	 * @return true if the current day is a working day, false otherwise
	 */
	private boolean isWorkingDay() {
		LocalDate today = LocalDate.now();
		return !DateTimeUtils.getHolidays().contains(today) && today.getDayOfWeek().getValue() <= 5;
	}

	/**
	 * Fetches data from Bitrix24 API and parses it into a Map.
	 *
	 * @return Map containing parsed data from Bitrix24
	 * @throws Exception if there's an error fetching or parsing the data
	 */
	private Map<String, Object> fetchAndParseData() throws Exception {
		String jsonData = apiClient.fetchBitrix24Data();
		Map<String, Object> data = objectMapper.readValue(jsonData, new TypeReference<Map<String, Object>>() {
		});
		logger.info("Received data: {}", data);
		return data;
	}

	/**
	 * Updates Google Sheets with the provided data.
	 *
	 * @param data Map containing the data to be updated in Google Sheets
	 * @throws Exception if there's an error updating Google Sheets
	 */
	private void updateGoogleSheets(Map<String, Object> data) throws Exception {
	    Objects.requireNonNull(data, "Data cannot be null");
	    if (data.isEmpty()) {
	        logger.warn("Attempting to update Google Sheets with empty data");
	    }
		logger.info("Updating Google Sheets...");
		sheetsService.updateSheet(data);
		logger.info("Google Sheets update completed");
	}

	/**
	 * Notifies the admin about successful job execution.
	 */
	private void notifySuccess() {
		ErrorNotificationHelper.notifyAdminAboutSuccess("Bitrix24Job");
	}

	/**
	 * Handles errors that occur during job execution.
	 *
	 * @param e the Exception that occurred
	 * @throws JobExecutionException wrapping the original exception
	 */
	private void handleJobExecutionError(Exception e) throws JobExecutionException {
	    Objects.requireNonNull(e, "Exception cannot be null");
		logAndNotifyError(ERROR_EXECUTING_JOB, e);
		throw new JobExecutionException(e);
	}

	/**
	 * Logs an error message and notifies the admin about the error.
	 *
	 * @param message the error message to log and send
	 * @param e the Exception that occurred
	 */
	private void logAndNotifyError(String message, Exception e) {
	    Objects.requireNonNull(message, "Error message cannot be null");
	    Objects.requireNonNull(e, "Exception cannot be null");
		logger.error(message, e);
		ErrorNotificationHelper.notifyAdminAboutError(CLASS_NAME, METHOD_NAME, message, e);
	}
}