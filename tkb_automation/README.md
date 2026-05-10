# TKB PTIT - Selenium Automation Test Framework

Automated test suite for the PTIT Timetable Scheduler (TKB PTIT) system, covering 566 system test cases across 7 modules.

## Project Structure

```
tkb_automation/
 config/              # Configuration files
   config.yaml        # Main config (URLs, DB, credentials)
 data/               # Test data Excel files
   test_data_rooms.xlsx
   test_data_semesters.xlsx
   test_data_scheduling.xlsx
   test_data_ctdt.xlsx
   test_data_users.xlsx
   test_data_tkb.xlsx
   test_data_hvk.xlsx
 pages/               # Page Object Models
   base_page.py       # Base POM with shared Selenium methods
   login_page.py
   rooms_page.py      # Room Management (PH-01 to PH-93)
   semesters_page.py  # Semester Management (HK-01 to HK-66)
   scheduling_page.py  # Scheduling (LL-01 to LL-134)
   ctdt_page.py      # Training Program (CTDT-01 to CTDT-73)
   users_page.py      # User Management (QLND-01 to QLND-41)
   tkb_page.py       # Timetable Management (TKB-01 to TKB-75)
   hvk_page.py       # Post-Validation (HVK-01 to HVK-84)
 tests/               # Test cases
   conftest.py       # pytest fixtures & hooks
   test_rooms.py     # 93 room tests
   test_semesters.py  # 66 semester tests
   test_scheduling.py # 134 scheduling tests
   test_ctdt.py      # 73 CTDT tests
   test_users.py     # 41 user tests
   test_tkb.py       # 75 TKB tests
   test_hvk.py       # 84 HVK tests
 core/                # Framework core
   browser_factory.py  # WebDriver setup
   db_helper.py       # MySQL with transaction/rollback
   api_helper.py      # REST API client
   excel_reader.py    # Excel data reader
   test_data_loader.py # Test data provider
   screenshot_util.py  # Screenshot capture
   assert_helper.py   # Custom assertions
   report_generator.py # HTML report generation
   config_loader.py   # YAML config loader
 reports/              # Output reports
 logs/                 # Test logs
 requirements.txt     # Python dependencies
 pytest.ini          # pytest configuration
 run_tests.py        # Main test runner
```

## Setup

### 1. Install Dependencies

```bash
cd E:/SQA_01_14/tkb_automation
pip install -r requirements.txt
```

### 2. Configure

Edit `config/config.yaml`:

```yaml
app:
  base_url: "http://localhost:3000"    # React frontend
  api_base_url: "http://localhost:8081/api/v1"  # Spring Boot backend
  headless: false

database:
  host: "localhost"
  port: 3306
  user: "root"
  password: "your_password"
  database: "schedule"

auth:
  username: "admin"
  password: "admin123"

browser:
  default: "chrome"
```

### 3. Ensure Application is Running

Start the backend:
```bash
cd E:/SQA_01_14/timetable-scheduler-ptit
mvn spring-boot:run
```

Start the frontend:
```bash
cd E:/SQA_01_14/tkb-ptit-react
npm run dev
```

## Running Tests

### Run all tests (single-threaded)
```bash
python run_tests.py
```

### Run specific module
```bash
python run_tests.py --module rooms       # Room tests (PH-01 to PH-93)
python run_tests.py --module semesters  # Semester tests (HK-01 to HK-66)
python run_tests.py --module scheduling # Scheduling tests (LL-01 to LL-134)
python run_tests.py --module ctdt       # Training program tests
python run_tests.py --module users      # User management tests
python run_tests.py --module tkb       # Timetable management tests
python run_tests.py --module hvk       # Post-validation tests
```

### Run with parallel execution
```bash
python run_tests.py --module all --parallel 4
```

### Run with HTML report
```bash
python run_tests.py --module all --html
```

### Run with custom report
```bash
python run_tests.py --module all --generate-report
```

### Run specific markers
```bash
python run_tests.py --module all --markers "ui"
python run_tests.py --module rooms --markers "negative"
python run_tests.py --module rooms --markers "db_write"
```

### Use Firefox
```bash
python run_tests.py --browser firefox --module rooms
```

### Dry run (collect only)
```bash
python run_tests.py --module rooms --dry-run
```

## Test Coverage

| Module | Test Cases | Coverage |
|--------|-----------|----------|
| QL Phong hoc (Rooms) | 93 | PH-01 to PH-93 |
| QL Hoc ki (Semesters) | 66 | HK-01 to HK-66 |
| Lap lich (Scheduling) | 134 | LL-01 to LL-134 |
| QL CTDT (Training Program) | 73 | CTDT-01 to CTDT-73 |
| QL Nguoi dung (Users) | 41 | QLND-01 to QLND-41 |
| QL TKB (Timetable) | 75 | TKB-01 to TKB-75 |
| Hau kiem (Post-Validation) | 84 | HVK-01 to HVK-84 |
| **Total** | **566** | |

## Features

- **Page Object Model**: Maintainable Selenium POM for all 7 modules
- **Data-Driven Testing**: All test cases stored in Excel files
- **Database Verification**: Direct MySQL connection with auto-rollback
- **API Verification**: REST API backup verification via `requests`
- **Screenshot on Failure**: Auto-capture on every failed assertion
- **Custom HTML Report**: Styled report with pass rates, charts, and test details
- **Parallel Execution**: pytest-xdist for multi-worker test runs
- **Markers & Filtering**: Run subsets by module, type (UI/functional/negative/e2e)

## Test Data Files

Each module has a corresponding Excel file in `data/`:

| File | Test Cases | Columns |
|-------|-----------|---------|
| `test_data_rooms.xlsx` | PH-01 to PH-93 | TC_ID, Module, Test_Type, Description, Precondition, Test_Steps, Input_Data, Expected_Result, Check_DB, Rollback, Priority, Auto_Executable |
| `test_data_semesters.xlsx` | HK-01 to HK-66 | Same structure |
| `test_data_scheduling.xlsx` | LL-01 to LL-134 | Same structure |
| `test_data_ctdt.xlsx` | CTDT-01 to CTDT-73 | Same structure |
| `test_data_users.xlsx` | QLND-01 to QLND-41 | Same structure |
| `test_data_tkb.xlsx` | TKB-01 to TKB-75 | Same structure |
| `test_data_hvk.xlsx` | HVK-01 to HVK-84 | Same structure |

To regenerate test data Excel files:

```bash
python generate_test_data.py
```

## Architecture

```
Browser (Chrome/Firefox/Edge)
  -> LoginPage (auto-authenticate)
    -> ModulePage (rooms_page, semesters_page, scheduling_page, etc.)
      -> Selenium actions (click, type, select, etc.)
      -> DB verification via DBHelper (rollback after each write test)
      -> API verification via APIHelper (backup verification)
      -> Screenshot on failure (auto-attached to report)
```

## Key Classes

- `DBHelper`: MySQL connection with transaction/rollback for write tests
- `APIHelper`: JWT-authenticated REST client for backend verification
- `ExcelReader`: Reads test data from Excel files with JSON column support
- `TestDataLoader`: Provides test data to pytest via parametrize
- `ReportGenerator`: Creates styled HTML reports with charts and details
- `AssertHelper`: Custom assertions that auto-screenshot on failure
