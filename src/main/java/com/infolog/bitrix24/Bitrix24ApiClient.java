package com.infolog.bitrix24;

import com.infolog.util.ConfigLoader;
import com.infolog.util.ErrorNotificationHelper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

/**
 * Client for interacting with the Bitrix24 API. This class is responsible for
 * making HTTP requests to the Bitrix24 API and retrieving data.
 */
public class Bitrix24ApiClient {
	private static final Logger logger = LoggerFactory.getLogger(Bitrix24ApiClient.class);
	private static final String API_URL_PROPERTY = "bitrix24.api.url";
	private static final String ERROR_FETCHING_DATA = "Error fetching data from Bitrix24 API";

	private final String apiUrl;
	private final OkHttpClient client;

	/**
	 * Constructs a new Bitrix24ApiClient. Initializes the API URL and HTTP client.
	 *
	 * @throws IllegalStateException if the API URL is not configured properly
	 */
	public Bitrix24ApiClient() {
		this.apiUrl = Objects.requireNonNull(ConfigLoader.getProperty(API_URL_PROPERTY),
				"Bitrix24 API URL is not configured properly");
		this.client = new OkHttpClient();
		logger.info("Bitrix24ApiClient initialized with API URL: {}", apiUrl);
	}

	/**
	 * Fetches data from the Bitrix24 API.
	 *
	 * @return The response body as a String
	 * @throws IOException if there's an error during the HTTP request or response
	 *                     handling
	 */
	public String fetchBitrix24Data() throws IOException {
		Request request = new Request.Builder().url(apiUrl).build();

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				handleUnsuccessfulResponse(response);
			}

			String responseBody = getResponseBody(response);
			logger.debug("Received response: {}", responseBody);
			return responseBody;
		} catch (IOException e) {
			handleIOException(e);
			throw e;
		}
	}

	/**
	 * Handles unsuccessful HTTP responses.
	 *
	 * @param response The HTTP response
	 * @throws IOException with details about the unsuccessful response
	 */
	private void handleUnsuccessfulResponse(Response response) throws IOException {
		String errorMessage = String.format("Request failed with code: %d", response.code());
		logger.error(errorMessage);
		throw new IOException("Unexpected code " + response);
	}

	/**
	 * Extracts the response body from the HTTP response.
	 *
	 * @param response The HTTP response
	 * @return The response body as a String
	 * @throws IOException if there's an error reading the response body
	 */
	private String getResponseBody(Response response) throws IOException {
		ResponseBody body = response.body();
		if (body == null) {
			throw new IOException("Response body is null");
		}
		return body.string();
	}

	/**
	 * Handles IOExceptions that occur during the API request.
	 *
	 * @param e The caught IOException
	 */
	private void handleIOException(IOException e) {
		logger.error(ERROR_FETCHING_DATA, e);
		ErrorNotificationHelper.notifyAdminAboutError("Bitrix24ApiClient", "fetchBitrix24Data", ERROR_FETCHING_DATA, e);
	}
}