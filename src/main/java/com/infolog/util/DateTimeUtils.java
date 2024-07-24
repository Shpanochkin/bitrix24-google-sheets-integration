package com.infolog.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Утилитарный класс для работы с датами и праздниками. Предоставляет методы для
 * получения списка праздничных дней.
 */
public final class DateTimeUtils {
	private static final Logger logger = LoggerFactory.getLogger(DateTimeUtils.class);
	private static List<LocalDate> cachedHolidays;
	private static int cachedYear;
	private static final String API_URL = "https://isdayoff.ru/api/getdata?year=";
	private static final int API_TIMEOUT_SECONDS = 10;
	private static final String ERROR_FETCH_HOLIDAYS = "Ошибка при получении данных о праздниках для года: ";

	private DateTimeUtils() {
		throw new AssertionError("Утилитарный класс не должен быть инстанцирован");
	}

	/**
	 * Получает список праздничных дней для текущего года. Если список уже кэширован
	 * и год не изменился, возвращает кэшированный список. В противном случае,
	 * запрашивает новый список праздников и кэширует его.
	 *
	 * @return неизменяемый список праздничных дней
	 */
	public static List<LocalDate> getHolidays() {
		int currentYear = LocalDate.now().getYear();
		if (cachedHolidays == null || cachedYear != currentYear) {
			updateHolidayCache(currentYear);
		}
		return cachedHolidays;
	}

	/**
	 * Обновляет кэш праздников для указанного года.
	 *
	 * @param year год, для которого нужно обновить список праздников
	 */
	private static synchronized void updateHolidayCache(int year) {
		if (cachedYear != year) {
			List<LocalDate> holidays = fetchHolidaysFromApi(year);
			cachedHolidays = Collections.unmodifiableList(holidays);
			cachedYear = year;
			logger.info("Обновлен кэш праздников для года: {}", year);
		}
	}

	/**
	 * Запрашивает список праздников с API для указанного года. Если запрос
	 * неуспешен, возвращает список праздников по умолчанию.
	 *
	 * @param year год, для которого нужно получить список праздников
	 * @return список праздничных дней
	 */
	static List<LocalDate> fetchHolidaysFromApi(int year) {
		Objects.requireNonNull(year, "Год не может быть null");
		if (year < 1900 || year > 2100) {
			throw new IllegalArgumentException("Год должен быть между 1900 и 2100");
		}

		List<LocalDate> holidays = new ArrayList<>();
		String apiUrl = API_URL + year;

		try {
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl))
					.timeout(Duration.ofSeconds(API_TIMEOUT_SECONDS)).build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 200) {
				processApiResponse(response.body(), year, holidays);
			} else {
				handleApiError(response.statusCode());
			}
		} catch (Exception e) {
			handleApiException(year, e);
		}

		if (holidays.isEmpty()) {
			logger.warn("Не удалось получить данные о праздниках. Используем данные по умолчанию для года: {}", year);
			holidays = getDefaultHolidays(year);
		}

		return holidays;
	}

	/**
	 * Обрабатывает ответ API и заполняет список праздников.
	 *
	 * @param responseBody тело ответа API
	 * @param year         год, для которого обрабатываются данные
	 * @param holidays     список для заполнения праздничными днями
	 */
	private static void processApiResponse(String responseBody, int year, List<LocalDate> holidays) {
		for (int i = 0; i < responseBody.length(); i++) {
			if (responseBody.charAt(i) == '1') {
				LocalDate date = LocalDate.ofYearDay(year, i + 1);
				if (date.getDayOfWeek().getValue() <= 5) {
					holidays.add(date);
				}
			}
		}
		logger.info("Успешно получены данные о праздниках для года: {}", year);
	}

	/**
	 * Обрабатывает ошибку API.
	 *
	 * @param statusCode код статуса HTTP ответа
	 */
	private static void handleApiError(int statusCode) {
		ErrorNotificationHelper.notifyAdminAboutError("DateTimeUtils", "fetchHolidaysFromApi",
				"Ошибка при получении данных о праздниках: HTTP error code " + statusCode, null);
	}

	/**
	 * Обрабатывает исключение, возникшее при запросе к API.
	 *
	 * @param year год, для которого выполнялся запрос
	 * @param e    исключение, которое произошло
	 */
	private static void handleApiException(int year, Exception e) {
		ErrorNotificationHelper.notifyAdminAboutError("DateTimeUtils", "fetchHolidaysFromApi",
				ERROR_FETCH_HOLIDAYS + year, e);
	}

	/**
	 * Возвращает список праздников по умолчанию для указанного года.
	 *
	 * @param year год, для которого нужно получить список праздников
	 * @return список праздничных дней по умолчанию
	 */
	static List<LocalDate> getDefaultHolidays(int year) {
		return Arrays.asList(LocalDate.of(year, 1, 1), LocalDate.of(year, 1, 2), LocalDate.of(year, 1, 3),
				LocalDate.of(year, 1, 4), LocalDate.of(year, 1, 5), LocalDate.of(year, 1, 6), LocalDate.of(year, 1, 7),
				LocalDate.of(year, 1, 8), LocalDate.of(year, 2, 23), LocalDate.of(year, 3, 8), LocalDate.of(year, 5, 1),
				LocalDate.of(year, 5, 9), LocalDate.of(year, 6, 12), LocalDate.of(year, 11, 4));
	}

	public static LocalDate now() {
		return LocalDate.now();
	}
}