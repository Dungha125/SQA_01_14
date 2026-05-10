"""pytest configuration, fixtures, and hooks for TKB Automation Framework."""
from __future__ import annotations

import os
import sys
from pathlib import Path
from typing import TYPE_CHECKING, Generator

import pytest
from selenium.webdriver.remote.webdriver import WebDriver

# Ensure project root is in path
sys.path.insert(0, str(Path(__file__).parent.parent))

from core.browser_factory import BrowserFactory
from core.config_loader import config
from core.db_helper import DBHelper
from core.api_helper import APIHelper
from core.screenshot_util import screenshot_util
from pages.login_page import LoginPage
from pages.rooms_page import RoomsPage
from pages.semesters_page import SemestersPage
from pages.scheduling_page import SchedulingPage
from pages.ctdt_page import CTDTPage
from pages.users_page import UsersPage
from pages.tkb_page import TKBPage
from pages.hvk_page import HVKPage

if TYPE_CHECKING:
    pass


# =========================================================================
# Session-scoped fixtures (shared across all tests)
# =========================================================================

@pytest.fixture(scope="session")
def browser() -> Generator[WebDriver, None, None]:
    """Create a browser WebDriver instance for the entire test session.

    Set BROWSER=chrome|firefox|edge and HEADLESS=true|false to configure.
    """
    driver = BrowserFactory.create_driver()
    yield driver
    BrowserFactory.quit_driver(driver)


@pytest.fixture(scope="session")
def api_client() -> Generator[APIHelper, None, None]:
    """Create an authenticated API client for the entire session."""
    client = APIHelper()
    client.login()
    yield client
    client.close()


# =========================================================================
# Function-scoped fixtures (fresh for each test)
# =========================================================================

@pytest.fixture(scope="function")
def driver() -> Generator[WebDriver, None, None]:
    """Provide a fresh browser instance for each test with session recovery."""
    from core.browser_factory import BrowserFactory
    driver_instance = BrowserFactory.create_driver()
    driver_instance.set_window_size(config.window_width, config.window_height)
    yield driver_instance
    try:
        driver_instance.execute_script("window.localStorage.clear();")
        driver_instance.delete_all_cookies()
    except Exception:
        pass
    try:
        BrowserFactory.quit_driver(driver_instance)
    except Exception:
        pass


@pytest.fixture(scope="function")
def db() -> Generator[DBHelper, None, None]:
    """Provide a database helper with auto-rollback after each test."""
    db_helper = DBHelper()
    try:
        db_helper.connect()
        db_helper.begin()
        yield db_helper
    finally:
        db_helper.rollback()
        db_helper.disconnect()


@pytest.fixture(scope="function")
def login(driver: WebDriver) -> LoginPage:
    """Log in as admin and return the login page object.

    After this fixture runs, the browser is on the dashboard/home page.
    """
    login_page = LoginPage(driver)
    login_page.open_page()
    login_page.login(config.auth_username, config.auth_password)
    return login_page


@pytest.fixture(scope="function")
def rooms_page(driver: WebDriver) -> RoomsPage:
    """Return an authenticated RoomsPage instance."""
    login_page = LoginPage(driver)
    login_page.open_page()
    login_page.login(config.auth_username, config.auth_password)
    page = RoomsPage(driver)
    page.open_page()
    return page


@pytest.fixture(scope="function")
def semesters_page(driver: WebDriver) -> SemestersPage:
    """Return an authenticated SemestersPage instance."""
    login_page = LoginPage(driver)
    login_page.open_page()
    login_page.login(config.auth_username, config.auth_password)
    page = SemestersPage(driver)
    page.open_page()
    return page


@pytest.fixture(scope="function")
def scheduling_page(driver: WebDriver) -> SchedulingPage:
    """Return an authenticated SchedulingPage instance."""
    login_page = LoginPage(driver)
    login_page.open_page()
    login_page.login(config.auth_username, config.auth_password)
    page = SchedulingPage(driver)
    page.open_page()
    return page


@pytest.fixture(scope="function")
def ctdt_page(driver: WebDriver) -> CTDTPage:
    """Return an authenticated CTDTPage instance."""
    login_page = LoginPage(driver)
    login_page.open_page()
    login_page.login(config.auth_username, config.auth_password)
    page = CTDTPage(driver)
    page.open_page()
    return page


@pytest.fixture(scope="function")
def users_page(driver: WebDriver) -> UsersPage:
    """Return an authenticated UsersPage instance."""
    login_page = LoginPage(driver)
    login_page.open_page()
    login_page.login(config.auth_username, config.auth_password)
    page = UsersPage(driver)
    page.open_page()
    return page


@pytest.fixture(scope="function")
def tkb_page(driver: WebDriver) -> TKBPage:
    """Return an authenticated TKBPage instance."""
    login_page = LoginPage(driver)
    login_page.open_page()
    login_page.login(config.auth_username, config.auth_password)
    page = TKBPage(driver)
    page.open_page()
    return page


@pytest.fixture(scope="function")
def hvk_page(driver: WebDriver) -> HVKPage:
    """Return an authenticated HVKPage instance."""
    login_page = LoginPage(driver)
    login_page.open_page()
    login_page.login(config.auth_username, config.auth_password)
    page = HVKPage(driver)
    page.open_page()
    return page


# =========================================================================
# Test result hooks - screenshot on failure
# =========================================================================

def pytest_runtest_makereport(item, call):
    """Capture screenshot when a test fails."""
    if call.excinfo is not None and call.when == "call":
        driver = None
        try:
            driver = item.funcargs.get("driver")
        except Exception:
            pass
        if driver is None:
            try:
                browser = item.funcargs.get("browser")
                if browser:
                    driver = browser
            except Exception:
                pass
        if driver is not None:
            test_name = item.name
            module = _get_module_name(item)
            screenshot_util.capture_on_failure(driver, test_name, module)


def _get_module_name(item) -> str:
    """Extract module name from test item."""
    if "test_rooms" in item.nodeid:
        return "rooms"
    if "test_semesters" in item.nodeid:
        return "semesters"
    if "test_scheduling" in item.nodeid:
        return "scheduling"
    if "test_ctdt" in item.nodeid:
        return "ctdt"
    if "test_users" in item.nodeid:
        return "users"
    if "test_tkb" in item.nodeid:
        return "tkb"
    if "test_hvk" in item.nodeid:
        return "hvk"
    return "unknown"


# =========================================================================
# pytest configuration hooks
# =========================================================================

def pytest_configure(config) -> None:
    """Register custom markers."""
    config.addinivalue_line("markers", "rooms: Room management tests (PH)")
    config.addinivalue_line("markers", "semesters: Semester management tests (HK)")
    config.addinivalue_line("markers", "scheduling: Scheduling tests (LL)")
    config.addinivalue_line("markers", "ctdt: Training program tests (CTDT)")
    config.addinivalue_line("markers", "users: User management tests (QLND)")
    config.addinivalue_line("markers", "tkb: Timetable management tests (TKB)")
    config.addinivalue_line("markers", "hvk: Post-validation tests (HVK)")
    config.addinivalue_line("markers", "ui: UI/UX tests")
    config.addinivalue_line("markers", "functional: Functional tests")
    config.addinivalue_line("markers", "negative: Negative/invalid input tests")
    config.addinivalue_line("markers", "e2e: End-to-end tests")
    config.addinivalue_line("markers", "slow: Slow-running tests")
    config.addinivalue_line("markers", "db_write: Tests that modify database")


def pytest_collection_modifyitems(config, items) -> None:
    """Auto-apply markers based on test file and test type."""
    for item in items:
        node_id = item.nodeid.lower()
        if "test_rooms" in node_id:
            item.add_marker(pytest.mark.rooms)
        elif "test_semesters" in node_id:
            item.add_marker(pytest.mark.semesters)
        elif "test_scheduling" in node_id:
            item.add_marker(pytest.mark.scheduling)
        elif "test_ctdt" in node_id:
            item.add_marker(pytest.mark.ctdt)
        elif "test_users" in node_id:
            item.add_marker(pytest.mark.users)
        elif "test_tkb" in node_id:
            item.add_marker(pytest.mark.tkb)
        elif "test_hvk" in node_id:
            item.add_marker(pytest.mark.hvk)
        if "ui" in node_id or "_ui_" in node_id:
            item.add_marker(pytest.mark.ui)
        if "negative" in node_id or "neg" in node_id:
            item.add_marker(pytest.mark.negative)
        if "e2e" in node_id or "_e2e_" in node_id or "end_to_end" in node_id:
            item.add_marker(pytest.mark.e2e)
        if "functional" not in item.keywords:
            item.add_marker(pytest.mark.functional)


def pytest_terminal_summary(terminalreporter, exitstatus, config) -> None:
    """Add a custom summary section to the terminal output."""
    print("\n" + "=" * 70)
    print("TKB AUTOMATION FRAMEWORK - TEST SUMMARY")
    print("=" * 70)
    stats = terminalreporter.stats
    total = sum(len(v) for v in stats.values())
    passed = len(stats.get("passed", []))
    failed = len(stats.get("failed", []))
    skipped = len(stats.get("skipped", []))
    pass_rate = (passed / total * 100) if total > 0 else 0
    print(f"  Total:   {total}")
    print(f"  Passed:  {passed}")
    print(f"  Failed:  {failed}")
    print(f"  Skipped: {skipped}")
    print(f"  Pass Rate: {pass_rate:.1f}%")
    print("=" * 70)
    screenshot_dir = screenshot_util.screenshot_dir
    print(f"  Screenshots saved to: {screenshot_dir}")
    print("=" * 70)
