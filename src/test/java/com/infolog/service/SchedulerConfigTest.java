package com.infolog.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the SchedulerConfig class using JUnit Jupiter.
 */
@ExtendWith(MockitoExtension.class)
class SchedulerConfigTest {

	@Mock
	private Scheduler mockScheduler;

	@Mock
	private Logger mockLogger;

	private SchedulerConfig config;

	/**
	 * Sets up the test environment before each test.
	 *
	 * @throws SchedulerException if there's an error setting up the mock scheduler
	 */
	@BeforeEach
	void setUp() throws SchedulerException {
		try (MockedStatic<StdSchedulerFactory> mockedStatic = mockStatic(StdSchedulerFactory.class)) {
			mockedStatic.when(StdSchedulerFactory::getDefaultScheduler).thenReturn(mockScheduler);
			config = new SchedulerConfig();
		}
	}

	/**
	 * Tests the constructor of SchedulerConfig.
	 */
	@Test
	void testConstructor() {
		assertNotNull(config);
	}

	/**
	 * Tests the startScheduler method of SchedulerConfig.
	 *
	 * @throws SchedulerException if there's an error starting the scheduler
	 */
	@Test
	void testStartScheduler() throws SchedulerException {
		config.startScheduler();
		verify(mockScheduler).start();
	}

	/**
	 * Tests the scheduleJob method of SchedulerConfig.
	 *
	 * @throws SchedulerException if there's an error scheduling the job
	 */
	@Test
	void testScheduleJob() throws SchedulerException {
		config.scheduleJob(TestJob.class);
		verify(mockScheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
	}

	/**
	 * Tests the shutdown method of SchedulerConfig.
	 *
	 * @throws SchedulerException if there's an error shutting down the scheduler
	 */
	@Test
	void testShutdown() throws SchedulerException {
		when(mockScheduler.isShutdown()).thenReturn(false);
		config.shutdown();
		verify(mockScheduler).shutdown();
	}

	/**
	 * Tests that the scheduleJob method creates a trigger that runs daily at 20:55.
	 *
	 * @throws SchedulerException if there's an error scheduling the job
	 */
	@Test
	void testScheduleJobCreatesCorrectTrigger() throws SchedulerException {
		config.scheduleJob(TestJob.class);
		verify(mockScheduler).scheduleJob(
				argThat(job -> job.getKey().getName().equals("TestJob") && job.getKey().getGroup().equals("group1")
						&& job.getJobClass().equals(TestJob.class)),
				argThat(trigger -> trigger instanceof CronTrigger && trigger.getKey().getName().equals("TestJobTrigger")
						&& trigger.getKey().getGroup().equals("group1")
						&& ((CronTrigger) trigger).getCronExpression().equals("0 55 20 ? * *")));
	}

	/**
	 * Test job class used for scheduling tests.
	 */
	public static class TestJob implements Job {
		@Override
		public void execute(JobExecutionContext context) {
			// Do nothing, this is just a test job
		}
	}
}