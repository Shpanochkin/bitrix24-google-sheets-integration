package com.infolog.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Utility class for handling error notifications and logging.
 * This class provides methods to notify administrators about errors and log error details.
 */
public final class ErrorNotificationHelper {
    private static final Logger logger = LoggerFactory.getLogger(ErrorNotificationHelper.class);
    private static final String DEFAULT_ERROR_MESSAGE = "Unknown error occurred";
    private static final String ERROR_MESSAGE_FORMAT = "Error in %s.%s: %s%nDetails: %s";
    private static final String SUCCESS_MESSAGE_FORMAT = "Job %s completed successfully";

    /**
     * Notifies the admin about an error and logs the error details.
     *
     * @param className    the name of the class where the error occurred
     * @param methodName   the name of the method where the error occurred
     * @param errorMessage a custom error message describing the error
     * @param e            the Throwable object representing the error, can be null
     * @throws IllegalArgumentException if className or methodName is null or empty
     */
    public static void notifyAdminAboutError(String className, String methodName, String errorMessage, Throwable e) {
        validateInput(className, "className");
        validateInput(methodName, "methodName");

        String safeErrorMessage = (errorMessage != null) ? errorMessage : DEFAULT_ERROR_MESSAGE;
        String fullErrorMessage = formatErrorMessage(className, methodName, safeErrorMessage, e);

        notifyAdmin(fullErrorMessage);
        logError(fullErrorMessage, e);
    }
    
    /**
     * Notifies the admin about the successful completion of a job.
     *
     * @param jobName The name of the job that completed successfully. Must not be null or empty.
     * @throws IllegalArgumentException if jobName is null or empty
     */
    public static void notifyAdminAboutSuccess(String jobName) {
        if (jobName == null || jobName.trim().isEmpty()) {
            throw new IllegalArgumentException("Job name must not be null or empty");
        }

        String successMessage = String.format(SUCCESS_MESSAGE_FORMAT, jobName);
        logger.info(successMessage);

        try {
            TelegramNotificationService.sendNotification(successMessage);
        } catch (Exception e) {
            logger.error("Failed to send success notification for job: {}", jobName, e);
        }
    }

    /**
     * Validates that the input string is not null or empty.
     *
     * @param input the string to validate
     * @param paramName the name of the parameter being validated
     * @throws IllegalArgumentException if the input is null or empty
     */
    private static void validateInput(String input, String paramName) {
        Objects.requireNonNull(input, paramName + " must not be null");
        if (input.trim().isEmpty()) {
            throw new IllegalArgumentException(paramName + " must not be empty");
        }
    }

    /**
     * Formats the error message with the provided details.
     *
     * @param className    the name of the class where the error occurred
     * @param methodName   the name of the method where the error occurred
     * @param errorMessage a custom error message describing the error
     * @param e            the Throwable object representing the error, can be null
     * @return formatted error message
     */
    private static String formatErrorMessage(String className, String methodName, String errorMessage, Throwable e) {
        return String.format(ERROR_MESSAGE_FORMAT,
                className,
                methodName,
                errorMessage,
                e != null ? e.toString() : "No exception details");
    }

    /**
     * Notifies the admin about the error. This is a placeholder for future implementation.
     *
     * @param fullErrorMessage the complete error message to be sent to the admin
     */
    private static void notifyAdmin(String fullErrorMessage) {
    	TelegramNotificationService.sendNotification(fullErrorMessage);
        logger.info("Admin notification sent: {}", fullErrorMessage);
    }

    /**
     * Logs the error message and stack trace if available.
     *
     * @param fullErrorMessage the complete error message to be logged
     * @param e                the Throwable object representing the error, can be null
     */
    private static void logError(String fullErrorMessage, Throwable e) {
        if (e != null) {
            logger.error(fullErrorMessage, e);
        } else {
            logger.error(fullErrorMessage);
        }
    }

    /**
     * Private constructor to prevent instantiation of this utility class.
     *
     * @throws UnsupportedOperationException always, as this class should not be instantiated
     */
    private ErrorNotificationHelper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    
}