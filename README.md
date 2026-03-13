# Attendance Analyzer

## Project Overview
The Attendance Analyzer is a Spring Boot-based application designed to process and analyze employee attendance data from CSV files. The system automates the tracking of attendance violations, calculates work time metrics, and prepares data for payroll purposes by applying specific business rules for working hours and deductions.

## Key Features
- Multi-section CSV Parsing: Automatically detects date headers and maps employee records within a single file.
- Time Calculation Engine: Calculates net worked hours, short time, and extra time based on daily standards.
- Business Rule Enforcement: Automatically applies lunch break deductions and caps early clock-ins.
- Violation Detection: Identifies late arrivals, early departures, and missing punch records.
- RESTful API: Provides endpoints for file uploads and data processing.
- Weekend Exclusion: Automatically ignores Saturday and Sunday records from analysis.

## Technical Stack
- Language: Java 24
- Framework: Spring Boot 3.4.2
- Build Tool: Maven
- Parsing Library: OpenCSV 5.9
- Testing: JUnit 5, Mockito, AssertJ

## Business Rules
The system operates based on the following standard working hours:

### Working Hours
- Monday to Thursday: 8 hours 45 minutes (Standard day)
- Friday: 7 hours (Standard day)
- Flexible Clock-in Window: 07:30 to 08:00
- Earliest Countable Time: 07:30 (Any time worked before this is ignored)

### Deductions (Lunch Break)
- Monday to Thursday: 45 minutes deducted automatically
- Friday: 30 minutes deducted automatically

### Violation Logic
- Late Arrival: Any clock-in after 08:00
- Early Departure (Mon-Thu): Clock-out before 17:00
- Early Departure (Fri): Clock-out before 15:00
- Absent/Missing Punch: Calculated as lost hours based on standard daily minutes.

## Getting Started

### Prerequisites
- JDK 24 or higher
- Apache Maven 3.9+

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/Lifa-Mbangata/Attendance-Analyzer.git
   ```
2. Navigate to the project directory:
   ```bash
   cd Attendance-Analyzer
   ```
3. Build the project:
   ```bash
   mvn clean install
   ```

### Running the Application
To start the application, run the following command:
```bash
mvn spring-boot:run
```
The server will start on `http://localhost:8080` by default.

## API Documentation
### Upload Attendance File
- Endpoint: `POST /api/attendance/upload`
- Content-Type: `multipart/form-data`
- Parameter: `file` (CSV file)

### Reports
- Employee Summary: `GET /api/reports/employee/{id}`
- Department Summaries: `GET /api/reports/departments`
- Company Summary: `GET /api/reports/company`

## Testing
The project includes a comprehensive suite of unit and integration tests. To execute the tests, run:
```bash
mvn test
```
Tests cover:
- CSV parsing logic and date header detection.
- Time calculation accuracy across different weekdays.
- Edge cases (missing punches, leave comments, early clock-ins).
- Repository persistence and retrieval.
