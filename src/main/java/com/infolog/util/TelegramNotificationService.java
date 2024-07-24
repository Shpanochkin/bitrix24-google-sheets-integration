package com.infolog.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Service for sending notifications to a Telegram chat.
 */
public final class TelegramNotificationService {
	private static final Logger LOGGER = LoggerFactory.getLogger(TelegramNotificationService.class);
	private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot%s/sendMessage";
	private static final String CHAT_ID = "1166644090";
	private static final String BOT_TOKEN = ConfigLoader.getProperty("telegram.bot.token");
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String CONTENT_TYPE_VALUE = "application/x-www-form-urlencoded";
	private static final String REQUEST_BODY_FORMAT = "chat_id=%s&text=%s";
	private static final int HTTP_OK = 200;

	private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

	private TelegramNotificationService() {
		throw new UnsupportedOperationException("Utility class should not be instantiated");
	}

	/**
	 * Sends a notification message to the configured Telegram chat.
	 *
	 * @param message The message to send. Must not be null or empty.
	 * @throws IllegalArgumentException if the message is null or empty
	 */
	public static void sendNotification(String message) {
		Objects.requireNonNull(message, "Message must not be null");
		if (message.trim().isEmpty()) {
			throw new IllegalArgumentException("Message must not be empty");
		}

		String url = String.format(TELEGRAM_API_URL, BOT_TOKEN);
		String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
		String requestBody = String.format(REQUEST_BODY_FORMAT, CHAT_ID, encodedMessage);

		HttpRequest request = buildHttpRequest(url, requestBody);
		sendRequest(request);
	}

	/**
	 * Builds an HTTP request for sending a message to Telegram.
	 *
	 * @param url         The URL to send the request to
	 * @param requestBody The body of the request
	 * @return An HttpRequest object
	 */
	private static HttpRequest buildHttpRequest(String url, String requestBody) {
		return HttpRequest.newBuilder().uri(URI.create(url)).header(CONTENT_TYPE, CONTENT_TYPE_VALUE)
				.POST(HttpRequest.BodyPublishers.ofString(requestBody)).build();
	}

	/**
	 * Sends an HTTP request and handles the response.
	 *
	 * @param request The HttpRequest to send
	 */
	private static void sendRequest(HttpRequest request) {
		try {
			HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() != HTTP_OK) {
				LOGGER.error("Failed to send Telegram notification. Status code: {}", response.statusCode());
			} else {
				LOGGER.info("Telegram notification sent successfully");
			}
		} catch (IOException e) {
			LOGGER.error("IO error while sending Telegram notification", e);
		} catch (InterruptedException e) {
			LOGGER.error("Interrupted while sending Telegram notification", e);
			Thread.currentThread().interrupt();
		}
	}
}