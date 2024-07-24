package com.infolog.bitrix24;

import com.infolog.util.ConfigLoader;
import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the Bitrix24ApiClient class.
 */
@ExtendWith(MockitoExtension.class)
class Bitrix24ApiClientTest {

	private static final String TEST_API_URL = "https://test.bitrix24.com/rest/1/test_token/";
	private static final String TEST_RESPONSE = "Test response";

	@Mock
	private OkHttpClient mockClient;

	@Mock
	private Call mockCall;

	@Mock
	private Response mockResponse;

	private Bitrix24ApiClient apiClient;

	/**
	 * Sets up the test environment before each test.
	 */
	@BeforeEach
	void setUp() {
		try (MockedStatic<ConfigLoader> mockedConfigLoader = mockStatic(ConfigLoader.class)) {
			mockedConfigLoader.when(() -> ConfigLoader.getProperty("bitrix24.api.url")).thenReturn(TEST_API_URL);

			apiClient = new Bitrix24ApiClient();
			injectMockHttpClient();
		} catch (Exception e) {
			fail("Failed to set up test: " + e.getMessage());
		}
	}

	/**
	 * Injects the mock OkHttpClient into the apiClient using reflection.
	 *
	 * @throws Exception if reflection fails
	 */
	private void injectMockHttpClient() throws Exception {
		Field clientField = Bitrix24ApiClient.class.getDeclaredField("client");
		clientField.setAccessible(true);
		clientField.set(apiClient, mockClient);
	}

	/**
	 * Tests successful data fetching from Bitrix24 API.
	 *
	 * @throws IOException if an I/O error occurs
	 */
	@Test
	void testFetchBitrix24Data() throws IOException {
		setupSuccessfulResponse();

		String result = apiClient.fetchBitrix24Data();

		assertEquals(TEST_RESPONSE, result);
		verifyHttpClientInteractions();
	}

	/**
	 * Tests handling of unsuccessful response from Bitrix24 API.
	 */
	@Test
	void testFetchBitrix24DataUnsuccessfulResponse() throws IOException {
		setupUnsuccessfulResponse();

		assertThrows(IOException.class, () -> apiClient.fetchBitrix24Data());
	}

	/**
	 * Tests handling of null response body from Bitrix24 API.
	 */
	@Test
	void testFetchBitrix24DataNullBody() throws IOException {
		setupNullBodyResponse();

		assertThrows(IOException.class, () -> apiClient.fetchBitrix24Data());
	}

	private void setupSuccessfulResponse() throws IOException {
		when(mockClient.newCall(any())).thenReturn(mockCall);
		when(mockCall.execute()).thenReturn(mockResponse);
		when(mockResponse.isSuccessful()).thenReturn(true);
		when(mockResponse.body()).thenReturn(ResponseBody.create(TEST_RESPONSE, MediaType.get("application/json")));
	}

	private void setupUnsuccessfulResponse() throws IOException {
		when(mockClient.newCall(any())).thenReturn(mockCall);
		when(mockCall.execute()).thenReturn(mockResponse);
		when(mockResponse.isSuccessful()).thenReturn(false);
		when(mockResponse.code()).thenReturn(404);
	}

	private void setupNullBodyResponse() throws IOException {
		when(mockClient.newCall(any())).thenReturn(mockCall);
		when(mockCall.execute()).thenReturn(mockResponse);
		when(mockResponse.isSuccessful()).thenReturn(true);
		when(mockResponse.body()).thenReturn(null);
	}

	private void verifyHttpClientInteractions() throws IOException {
		verify(mockClient, times(1)).newCall(any());
		verify(mockCall, times(1)).execute();
	}
}