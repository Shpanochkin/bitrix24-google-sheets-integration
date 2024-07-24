package com.infolog;

import com.infolog.service.Bitrix24Job;
import com.infolog.service.SchedulerConfig;
import com.infolog.util.ConfigLoader;
import com.infolog.util.ErrorNotificationHelper;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class for the Bitrix24 Google Sheets Integration application.
 * This class is responsible for initializing and starting the scheduler,
 * as well as handling the application lifecycle.
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String APP_NAME_PROPERTY = "app.name";
    private static final String APP_VERSION_PROPERTY = "app.version";


    /**
     * The main method that starts the application.
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        logger.info("Starting {} version {}", 
            ConfigLoader.getProperty(APP_NAME_PROPERTY, "Unknown Application"), 
            ConfigLoader.getProperty(APP_VERSION_PROPERTY, "Unknown Version"));

        SchedulerConfig schedulerConfig = null;
        try {
            schedulerConfig = initializeAndStartScheduler();
            waitForApplicationToFinish();
        } catch (InterruptedException e) {
            handleInterruptedException(e);
        } catch (SchedulerException e) {
            handleSchedulerException(e);
        } catch (Exception e) {
            handleUnexpectedException(e);
        } finally {
            shutdownScheduler(schedulerConfig);
        }
    }

    /**
     * Initializes and starts the scheduler.
     *
     * @return Initialized SchedulerConfig object
     * @throws SchedulerException if there's an error in scheduler configuration or execution
     */
    private static SchedulerConfig initializeAndStartScheduler() throws SchedulerException {
        SchedulerConfig schedulerConfig = new SchedulerConfig();
        schedulerConfig.startScheduler();
        
        schedulerConfig.scheduleJob(Bitrix24Job.class);
        
        logger.info("Bitrix24Job scheduled to run daily at 17:05");
        logger.info("Application started successfully. The job will run according to the schedule. Press Ctrl+C to exit.");
        
        return schedulerConfig;
    }

    /**
     * Waits for the application to finish.
     *
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    private static void waitForApplicationToFinish() throws InterruptedException {
        Thread.currentThread().join();
    }

    /**
     * Handles InterruptedException.
     *
     * @param e The caught InterruptedException
     */
    private static void handleInterruptedException(InterruptedException e) {
        logger.info("Application interrupted", e);
        Thread.currentThread().interrupt();
    }

    /**
     * Handles SchedulerException.
     *
     * @param e The caught SchedulerException
     */
    private static void handleSchedulerException(SchedulerException e) {
        logger.error("Scheduler error", e);
        ErrorNotificationHelper.notifyAdminAboutError(
            "Main", 
            "main", 
            "Error in scheduler configuration or execution", 
            e
        );
    }

    /**
     * Handles unexpected exceptions.
     *
     * @param e The caught Exception
     */
    private static void handleUnexpectedException(Exception e) {
        logger.error("Unexpected error", e);
        ErrorNotificationHelper.notifyAdminAboutError(
            "Main", 
            "main", 
            "Critical error in main application", 
            e
        );
    }

    /**
     * Shuts down the scheduler.
     *
     * @param schedulerConfig The SchedulerConfig object to shut down
     */
    private static void shutdownScheduler(SchedulerConfig schedulerConfig) {
        if (schedulerConfig != null) {
            try {
                schedulerConfig.shutdown();
            } catch (SchedulerException e) {
                logger.error("Error shutting down scheduler", e);
                ErrorNotificationHelper.notifyAdminAboutError(
                    "Main", 
                    "shutdownScheduler", 
                    "Error shutting down scheduler", 
                    e
                );
            }
        }
        logger.info("Application shutting down");
    }
}