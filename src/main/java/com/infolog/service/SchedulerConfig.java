package com.infolog.service;

import com.infolog.util.ErrorNotificationHelper;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Configuration class for Quartz Scheduler. This class is responsible for
 * initializing, starting, and managing the Quartz Scheduler.
 */
public class SchedulerConfig {
	private static final Logger logger = LoggerFactory.getLogger(SchedulerConfig.class);
	private static final String DEFAULT_GROUP = "group1";
	private static final String CLASS_NAME = "SchedulerConfig";
	private static final String TRIGGER_SUFFIX = "Trigger";

	private final Scheduler scheduler;

	/**
	 * Constructs a new SchedulerConfig. Initializes the Quartz Scheduler.
	 *
	 * @throws SchedulerException if there's an error initializing the scheduler
	 */
	public SchedulerConfig() throws SchedulerException {
		try {
			this.scheduler = StdSchedulerFactory.getDefaultScheduler();
			logger.info("Scheduler initialized successfully");
		} catch (SchedulerException e) {
			logger.error("Error initializing scheduler", e);
			ErrorNotificationHelper.notifyAdminAboutError(CLASS_NAME, "constructor", "Error initializing scheduler", e);
			throw e;
		}
	}

	/**
	 * Starts the Quartz Scheduler.
	 *
	 * @throws SchedulerException if there's an error starting the scheduler
	 */
	public void startScheduler() throws SchedulerException {
		try {
			scheduler.start();
			logger.info("Scheduler started successfully");
		} catch (SchedulerException e) {
			logger.error("Error starting scheduler", e);
			ErrorNotificationHelper.notifyAdminAboutError(CLASS_NAME, "startScheduler", "Error starting scheduler", e);
			throw e;
		}
	}

	/**
	 * Schedules a job with the given job class and cron expression.
	 *
	 * @param jobClass       the Class object of the job to be scheduled
	 * @param cronExpression the cron expression for scheduling the job
	 * @throws SchedulerException       if there's an error scheduling the job
	 * @throws IllegalArgumentException if jobClass is null or cronExpression is
	 *                                  null or empty
	 */
	public void scheduleJob(Class<? extends Job> jobClass) throws SchedulerException {
		Objects.requireNonNull(jobClass, "Job class cannot be null");

		try {
			JobDetail job = JobBuilder.newJob(jobClass).withIdentity(jobClass.getSimpleName(), DEFAULT_GROUP).build();

			Trigger trigger = TriggerBuilder.newTrigger()
					.withIdentity(jobClass.getSimpleName() + TRIGGER_SUFFIX, DEFAULT_GROUP)
					.withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(20, 55)).build();

			scheduler.scheduleJob(job, trigger);
			logger.info("Cron expression for job {}: {}", jobClass.getSimpleName(),
					((CronTrigger) trigger).getCronExpression());
			logger.info("Job scheduled: {} to run daily at 20:55", jobClass.getSimpleName());
		} catch (SchedulerException e) {
			logger.error("Error scheduling job: {}", jobClass.getSimpleName(), e);
			ErrorNotificationHelper.notifyAdminAboutError(CLASS_NAME, "scheduleJob", "Error scheduling job", e);
			throw e;
		}
	}

	/**
	 * Shuts down the Quartz Scheduler.
	 *
	 * @throws SchedulerException if there's an error shutting down the scheduler
	 */
	public void shutdown() throws SchedulerException {
		if (scheduler != null && !scheduler.isShutdown()) {
			try {
				scheduler.shutdown();
				logger.info("Scheduler shut down successfully");
			} catch (SchedulerException e) {
				logger.error("Error shutting down scheduler", e);
				ErrorNotificationHelper.notifyAdminAboutError(CLASS_NAME, "shutdown", "Error shutting down scheduler",
						e);
				throw e;
			}
		}
	}
}