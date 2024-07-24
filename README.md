# Technical Documentation Bitrix24 Google Sheets Integration

## 1. System Overview
Bitrix24 Google Sheets Integration is a Java application designed to automate the process of extracting data from Bitrix24 CRM and subsequently entering it into Google Sheets reports on a daily basis. The application uses the Bitrix24 REST API to retrieve data and the Google Sheets API to write it.

## 2. Architecture
The application is built around the following key components:
- **Main**: Entry point of the application, initializes and starts the scheduler.
- **SchedulerConfig**: Quartz scheduler configuration for scheduled tasks.
- **Bitrix24Job**: Quartz job that performs the main logic of the application.
- **Bitrix24ApiClient**: Client for interacting with the Bitrix24 API.
- **GoogleSheetsService**: Service for working with the Google Sheets API.
- **ConfigLoader**: Utility for loading configuration parameters.
- **DateTimeUtils**: Utility for working with dates and holidays.
- **ErrorNotificationHelper**: Utility for sending error notifications.
- **TelegramNotificationService**: Service for sending notifications via Telegram.

## 3. Main Classes and Their Functionality

### 3.1. Main (Main.java)
The Main class is the entry point of the application. It is responsible for:
- Initializing and starting the scheduler
- Handling top-level exceptions
- Properly shutting down the application

### 3.2. SchedulerConfig (SchedulerConfig.java)
SchedulerConfig is responsible for configuring and managing the Quartz scheduler. Main functions:
- Initializing the Quartz scheduler
- Scheduling jobs
- Starting and stopping the scheduler

### 3.3. Bitrix24Job (Bitrix24Job.java)
Bitrix24Job is the main job of the application, executed on a schedule. It:
- Retrieves data from Bitrix24 via Bitrix24ApiClient
- Processes the retrieved data
- Updates Google Sheets via GoogleSheetsService
- Handles errors and sends notifications

### 3.4. Bitrix24ApiClient (Bitrix24ApiClient.java)
Bitrix24ApiClient handles interaction with the Bitrix24 API. Main functions:
- Formulating and sending HTTP requests to the Bitrix24 API
- Handling responses from the API
- Error handling when interacting with the API

### 3.5. GoogleSheetsService (GoogleSheetsService.java)
GoogleSheetsService provides functionality for working with the Google Sheets API. Key features:
- Authentication with the Google Sheets API
- Updating data in a specified Google Sheets spreadsheet
- Formatting data for insertion into the spreadsheet

### 3.6. ConfigLoader (ConfigLoader.java)
ConfigLoader is a utility class for loading configuration parameters from a file. Functions:
- Loading parameters from the config.properties file
- Providing access to configuration parameters for other classes

### 3.7. DateTimeUtils (DateTimeUtils.java)
DateTimeUtils provides utilities for working with dates and holidays. Main functions:
- Retrieving the list of holidays
- Determining if a day is a working day
- Caching the list of holidays to optimize performance

### 3.8. ErrorNotificationHelper (ErrorNotificationHelper.java)
ErrorNotificationHelper is responsible for sending error notifications. Functions:
- Formatting error messages
- Sending notifications via TelegramNotificationService
- Logging errors

### 3.9. TelegramNotificationService (TelegramNotificationService.java)
TelegramNotificationService provides functionality for sending notifications via Telegram. Main functions:
- Sending HTTP requests to the Telegram API
- Formatting messages for sending

## 4. Configuration
The application uses the config.properties file to store configuration parameters. Key parameters include:
- `bitrix24.api.url`: Bitrix24 API URL
- `google.sheets.spreadsheet.id`: Google Sheets spreadsheet ID
- `google.sheets.credentials.file.path`: Path to the Google Sheets credentials file
- `telegram.bot.token`: Telegram bot token for sending notifications

## 5. Execution Process
1. The application starts via the Main class.
2. SchedulerConfig initializes and starts the Quartz scheduler.
3. The scheduler executes Bitrix24Job daily at a specified time.
4. Bitrix24Job retrieves data via Bitrix24ApiClient.
5. The retrieved data is processed and formatted.
6. GoogleSheetsService updates data in Google Sheets.
7. In case of errors, ErrorNotificationHelper sends notifications via Telegram.

## 6. Error Handling and Logging
- The application uses SLF4J and Logback for logging.
- Errors are handled at every level of the application.
- Critical errors result in notifications being sent via Telegram.

## 7. Dependencies
Key project dependencies:
- OkHttp: for making HTTP requests
- Jackson: for JSON processing
- Google Sheets API: for working with Google Sheets
- Quartz: for job scheduling
- SLF4J and Logback: for logging

## 8. Build and Deployment
The project uses Maven for dependency management and build. Key commands:
- Build the project: `mvn clean package`
- Run tests: `mvn test`

To deploy:
- Configure the config.properties file
- Ensure access to Bitrix24 and Google Sheets APIs
- Run the application with the command `java -jar <jar_file_name>.jar`

## 9. Security
- Credentials are stored in a secure configuration file.
- HTTPS is used for all external API calls.
- Access to the Google Sheets API is restricted to necessary permissions only.

## 10. Performance and Scalability
- The application uses holiday caching to optimize performance.
- Quartz scheduler allows easy scaling of the number and frequency of tasks.
- Asynchronous request handling can be implemented if needed for performance improvement.

## 11. Future Development
Potential areas for improvement:
- Adding a web interface for managing tasks and viewing logs.
- Expanding functionality to work with other CRM systems.
- Implementing a more flexible reporting and data visualization system.

## 12. Bitrix24 SDK
The application uses the Bitrix24 SDK on the server-side to formulate responses to requests. Main components of the SDK include:

### 12.1. crest.php File
This file contains the CRest class, which provides methods for interacting with the Bitrix24 REST API. Key functions include:
- `call()`: Executes REST requests to the Bitrix24 API
- `callBatch()`: Executes batch requests
- `installApp()`: Installs the application
- `checkServer()`: Checks server settings

### 12.2. settings.php File
Contains key settings for working with the Bitrix24 API, including:
- `C_REST_WEB_HOOK_URL`: URL for the Bitrix24 webhook
- Additional settings for encoding, SSL ignoring, logging

### 12.3. Main Script
This script uses the SDK to execute requests to Bitrix24:
- Defines a set of filters to retrieve lead data
- Uses the `getLeads()` function to execute API requests
- Formats results in JSON for further processing

## 13. Data Transfer Process to Google Sheets
After receiving data from Bitrix24, it is transferred and processed in Google Sheets using Google Apps Script.

### 13.1. transferDataDaily() Function
This function performs the following operations:
- Opens the source and target Google Sheets.
- Determines the current date and month.
- Finds the corresponding sheet in the target spreadsheet based on the month and year.
- Determines the column for inserting data based on the current date.
- Copies data from specified cells of the source sheet to the corresponding cells of the target sheet.

Key features:
- Uses Russian month names for sheet naming.
- Automatically finds the correct column for data insertion based on the date.
- Logs the execution process for debugging.

## 14. Component Integration
- The Bitrix24 SDK on the server forms data in JSON format.
- The Java application (Bitrix24Job) retrieves this data via HTTP request.
- The retrieved data is processed and written to an intermediate Google Sheet.
- The Google Apps Script (transferDataDaily function) transfers data from the intermediate sheet to the final report daily.

## 15. Maintenance Recommendations
- Regularly check the logs of the Java application and the Google Apps Script for possible errors.
- Keep the Bitrix24 SDK updated as necessary.
- Periodically verify the proper functioning of the Bitrix24 webhook.
- Ensure that the Google Apps Script has the necessary permissions to access the sheets.

## 16. Possible Improvements
- Implement retry mechanisms for data retrieval or transfer failures.
- Add email or messenger notifications for critical errors.
- Implement an interface for monitoring the integration process and task status.

