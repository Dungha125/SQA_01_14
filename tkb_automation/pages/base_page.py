"""Base Page Object with shared Selenium utilities."""
from __future__ import annotations

import time
from typing import TYPE_CHECKING, Any

from selenium.common.exceptions import (
    ElementClickInterceptedException,
    ElementNotInteractableException,
    NoSuchElementException,
    StaleElementReferenceException,
    TimeoutException,
    WebDriverException,
)
from selenium.webdriver.common.action_chains import ActionChains
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.remote.webdriver import WebDriver
from selenium.webdriver.remote.webelement import WebElement
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.select import Select
from selenium.webdriver.support.ui import WebDriverWait

from core.config_loader import config

if TYPE_CHECKING:
    from selenium.webdriver.support.abstract_event_listener import AbstractEventListener


class BasePage:
    """Base Page Object with common Selenium interaction methods."""

    def __init__(self, driver: WebDriver) -> None:
        self.driver = driver
        self.timeout = config.timeout
        self._wait = WebDriverWait(driver, self.timeout)

    def open(self, url: str) -> None:
        """Navigate to a URL."""
        self.driver.get(url)

    def get_current_url(self) -> str:
        """Get the current URL."""
        return self.driver.current_url

    def get_title(self) -> str:
        """Get the page title."""
        return self.driver.title

    # --- Element finding ---
    def find_element(self, locator: tuple[str, str], timeout: int | None = None) -> WebElement:
        """Find a single element with explicit wait."""
        wait = WebDriverWait(self.driver, timeout or self.timeout)
        return wait.until(EC.presence_of_element_located(locator))

    def find_element_visible(self, locator: tuple[str, str], timeout: int | None = None) -> WebElement:
        """Find a visible element."""
        wait = WebDriverWait(self.driver, timeout or self.timeout)
        return wait.until(EC.visibility_of_element_located(locator))

    def find_element_clickable(self, locator: tuple[str, str], timeout: int | None = None) -> WebElement:
        """Find a clickable element."""
        wait = WebDriverWait(self.driver, timeout or self.timeout)
        return wait.until(EC.element_to_be_clickable(locator))

    def find_elements(self, locator: tuple[str, str]) -> list[WebElement]:
        """Find multiple elements."""
        return self.driver.find_elements(*locator)

    def wait_for_elements(self, locator: tuple[str, str], timeout: int | None = None,
                          min_count: int = 1) -> list[WebElement]:
        """Wait until minimum number of elements are present."""
        wait = WebDriverWait(self.driver, timeout or self.timeout)
        return wait.until(EC.presence_of_all_elements_located(locator))

    def is_element_present(self, locator: tuple[str, str]) -> bool:
        """Check if element exists in DOM."""
        try:
            self.driver.find_element(*locator)
            return True
        except NoSuchElementException:
            return False

    def is_element_visible(self, locator: tuple[str, str]) -> bool:
        """Check if element is visible."""
        try:
            el = self.driver.find_element(*locator)
            return el.is_displayed()
        except NoSuchElementException:
            return False

    def is_element_enabled(self, locator: tuple[str, str]) -> bool:
        """Check if element is enabled."""
        try:
            el = self.driver.find_element(*locator)
            return el.is_enabled()
        except NoSuchElementException:
            return False

    def wait_for_url_contains(self, text: str, timeout: int | None = None) -> bool:
        """Wait until URL contains text."""
        wait = WebDriverWait(self.driver, timeout or self.timeout)
        try:
            wait.until(EC.url_contains(text))
            return True
        except TimeoutException:
            return False

    def wait_for_title_contains(self, text: str, timeout: int | None = None) -> bool:
        """Wait until title contains text."""
        wait = WebDriverWait(self.driver, timeout or self.timeout)
        try:
            wait.until(EC.title_contains(text))
            return True
        except TimeoutException:
            return False

    # --- Element interactions ---
    def click(self, locator: tuple[str, str], timeout: int | None = None) -> None:
        """Click an element with wait."""
        el = self.find_element_clickable(locator, timeout)
        el.click()

    def double_click(self, locator: tuple[str, str]) -> None:
        """Double click an element."""
        el = self.find_element(locator)
        ActionChains(self.driver).double_click(el).perform()

    def right_click(self, locator: tuple[str, str]) -> None:
        """Right click an element."""
        el = self.find_element(locator)
        ActionChains(self.driver).context_click(el).perform()

    def type(self, locator: tuple[str, str], text: str, clear_first: bool = True,
             press_enter: bool = False) -> None:
        """Type text into an input field."""
        el = self.find_element_visible(locator)
        if clear_first:
            el.clear()
        el.send_keys(text)
        if press_enter:
            el.send_keys(Keys.RETURN)

    def type_slowly(self, locator: tuple[str, str], text: str) -> None:
        """Type text character by character."""
        el = self.find_element_visible(locator)
        el.clear()
        for char in text:
            el.send_keys(char)
            time.sleep(0.05)

    def press_key(self, locator: tuple[str, str], key: str) -> None:
        """Press a keyboard key."""
        el = self.find_element(locator)
        el.send_keys(key)

    def get_text(self, locator: tuple[str, str]) -> str:
        """Get the text content of an element."""
        el = self.find_element_visible(locator)
        return el.text.strip()

    def get_attribute(self, locator: tuple[str, str], attr: str) -> str | None:
        """Get an attribute value from an element."""
        try:
            el = self.find_element(locator)
            return el.get_attribute(attr)
        except NoSuchElementException:
            return None

    def get_inner_html(self, locator: tuple[str, str]) -> str | None:
        """Get inner HTML of an element."""
        return self.get_attribute(locator, "innerHTML")

    # --- Select / Dropdown ---
    def select_dropdown(self, locator: tuple[str, str], value: str | None = None,
                        index: int | None = None, text: str | None = None) -> None:
        """Select from a dropdown by value, index, or visible text."""
        el = self.find_element_visible(locator)
        select = Select(el)
        if value is not None:
            select.select_by_value(value)
        elif index is not None:
            select.select_by_index(index)
        elif text is not None:
            select.select_by_visible_text(text)

    def select_dropdown_element(self, element: WebElement, value: str | None = None,
                                 index: int | None = None, text: str | None = None) -> None:
        """Select from a WebElement dropdown by value, index, or visible text."""
        select = Select(element)
        if value is not None:
            select.select_by_value(value)
        elif index is not None:
            select.select_by_index(index)
        elif text is not None:
            select.select_by_visible_text(text)

    def type_element(self, element: WebElement, text: str,
                     clear_first: bool = True, press_enter: bool = False) -> None:
        """Type text directly into a WebElement."""
        if clear_first:
            element.clear()
        element.send_keys(text)
        if press_enter:
            element.send_keys(Keys.RETURN)

    def get_dropdown_options(self, locator: tuple[str, str]) -> list[str]:
        """Get all options from a dropdown."""
        el = self.find_element_visible(locator)
        select = Select(el)
        return [opt.text.strip() for opt in select.options]

    def get_selected_option(self, locator: tuple[str, str]) -> str:
        """Get the currently selected option text."""
        el = self.find_element_visible(locator)
        select = Select(el)
        return select.first_selected_option.text.strip()

    # --- Checkbox ---
    def check(self, locator: tuple[str, str]) -> None:
        """Check a checkbox if not already checked."""
        el = self.find_element(locator)
        if not el.is_selected():
            el.click()

    def uncheck(self, locator: tuple[str, str]) -> None:
        """Uncheck a checkbox if not already unchecked."""
        el = self.find_element(locator)
        if el.is_selected():
            el.click()

    def is_checked(self, locator: tuple[str, str]) -> bool:
        """Check if a checkbox is checked."""
        try:
            el = self.find_element(locator)
            return el.is_selected()
        except NoSuchElementException:
            return False

    # --- Scroll ---
    def scroll_to_element(self, locator: tuple[str, str]) -> None:
        """Scroll an element into view."""
        el = self.find_element(locator)
        self.driver.execute_script("arguments[0].scrollIntoView({block: 'center'});", el)

    def scroll_to_bottom(self) -> None:
        """Scroll to the bottom of the page."""
        self.driver.execute_script("window.scrollTo(0, document.body.scrollHeight);")

    def scroll_to_top(self) -> None:
        """Scroll to the top of the page."""
        self.driver.execute_script("window.scrollTo(0, 0);")

    def hover(self, locator: tuple[str, str]) -> None:
        """Hover over an element."""
        el = self.find_element(locator)
        ActionChains(self.driver).move_to_element(el).perform()

    # --- Alert / Modal ---
    def accept_alert(self, timeout: int | None = None) -> str:
        """Accept an alert and return its text."""
        wait = WebDriverWait(self.driver, timeout or self.timeout)
        alert = wait.until(EC.alert_is_present())
        text = alert.text
        alert.accept()
        return text

    def dismiss_alert(self, timeout: int | None = None) -> str:
        """Dismiss an alert and return its text."""
        wait = WebDriverWait(self.driver, timeout or self.timeout)
        alert = wait.until(EC.alert_is_present())
        text = alert.text
        alert.dismiss()
        return text

    def switch_to_modal(self, timeout: int | None = None) -> WebElement:
        """Switch to a modal dialog."""
        wait = WebDriverWait(self.driver, timeout or self.timeout)
        return wait.until(EC.visibility_of_element_located((By.CSS_SELECTOR, ".modal, [role='dialog'], .ant-modal, .Modal")))

    def close_modal(self, close_button: tuple[str, str] | None = None) -> None:
        """Close the current modal."""
        if close_button:
            self.click(close_button)
        else:
            try:
                close_btn = self.driver.find_element(By.CSS_SELECTOR, "[aria-label='Close'], .modal-header .close, button.close, [data-bs-dismiss='modal']")
                close_btn.click()
            except NoSuchElementException:
                pass

    # --- Toast / Notification ---
    def wait_for_toast(self, timeout: int | None = None, success: bool = True) -> str:
        """Wait for a toast notification and return its text."""
        wait = WebDriverWait(self.driver, timeout or self.timeout)
        selectors = [
            ".toast-success, .toast-error, .Toastify__toast, .ant-message, [class*='toast']",
            ".notification, .alert-success, .alert-error",
            "[role='alert']",
        ]
        for selector in selectors:
            try:
                el = wait.until(EC.visibility_of_element_located((By.CSS_SELECTOR, selector)))
                return el.text.strip()
            except TimeoutException:
                continue
        raise TimeoutException("Toast notification not found")

    def is_toast_visible(self, timeout: int = 3) -> bool:
        """Check if a toast message is visible."""
        selectors = [
            ".toast-success, .toast-error, .Toastify__toast, .ant-message",
            "[role='alert']",
        ]
        for selector in selectors:
            try:
                el = WebDriverWait(self.driver, timeout).until(EC.visibility_of_element_located((By.CSS_SELECTOR, selector)))
                return el.is_displayed()
            except TimeoutException:
                continue
        return False

    def get_toast_message(self, timeout: int = 3) -> str:
        """Get toast message text without waiting."""
        selectors = [
            ".toast-success, .toast-error, .Toastify__toast, .ant-message",
            "[role='alert']",
        ]
        for selector in selectors:
            try:
                el = self.driver.find_element(By.CSS_SELECTOR, selector)
                if el.is_displayed():
                    return el.text.strip()
            except NoSuchElementException:
                continue
        return ""

    # --- Pagination ---
    def click_page_number(self, page_num: int) -> None:
        """Click a specific page number in pagination."""
        locator = (By.XPATH, f"//button[contains(@class,'pagination')]//*[text()='{page_num}']")
        self.click(locator)

    def click_next_page(self) -> None:
        """Click the next page button."""
        next_btn = (By.CSS_SELECTOR, ".pagination .next, [aria-label='Next'], button:has-text('Tiếp')")
        self.click(next_btn)

    def click_prev_page(self) -> None:
        """Click the previous page button."""
        prev_btn = (By.CSS_SELECTOR, ".pagination .prev, [aria-label='Previous']")
        self.click(prev_btn)

    def get_pagination_label(self) -> str:
        """Get the pagination label text (e.g. 'Hiển thị 14 trên 98')."""
        selectors = [
            (By.CSS_SELECTOR, ".pagination-info, [class*='pagination'] span"),
            (By.XPATH, "//*[contains(text(),'Hiển thị')]"),
            (By.XPATH, "//*[contains(text(),'Showing')]"),
        ]
        for locator in selectors:
            try:
                return self.get_text(locator)
            except (NoSuchElementException, TimeoutException):
                continue
        return ""

    # --- Table ---
    def get_table_row_count(self, table_locator: tuple[str, str]) -> int:
        """Get the number of data rows in a table (excluding header)."""
        rows = self.driver.find_elements(f"{table_locator[1]} tbody tr, {table_locator[1]} > tr")
        return len([r for r in rows if r.is_displayed()])

    def get_table_data(self, table_locator: tuple[str, str]) -> list[list[str]]:
        """Get all table data as a 2D list."""
        data: list[list[str]] = []
        try:
            rows = self.driver.find_elements(By.CSS_SELECTOR, f"{table_locator[1]} tbody tr, {table_locator[1]} tr:not(:first-child)")
            for row in rows:
                cells = row.find_elements(By.TAG_NAME, "td")
                if cells:
                    data.append([cell.text.strip() for cell in cells])
        except NoSuchElementException:
            pass
        return data

    def get_column_index(self, table_locator: tuple[str, str], column_name: str) -> int:
        """Get the column index by header name."""
        headers = self.driver.find_elements(By.CSS_SELECTOR, f"{table_locator[1]} thead th, {table_locator[1]} th")
        for i, header in enumerate(headers):
            if column_name.lower() in header.text.lower():
                return i
        return -1

    # --- Screenshot ---
    def screenshot(self, name: str | None = None) -> str:
        """Take a screenshot and return the file path."""
        from core.screenshot_util import screenshot_util
        return screenshot_util.capture(self.driver, name or f"manual_{int(time.time())}")

    # --- Misc ---
    def refresh(self) -> None:
        """Refresh the page."""
        self.driver.refresh()

    def go_back(self) -> None:
        """Go back in browser history."""
        self.driver.back()

    def go_forward(self) -> None:
        """Go forward in browser history."""
        self.driver.forward()

    def wait(self, seconds: float) -> None:
        """Explicit wait."""
        time.sleep(seconds)

    def execute_script(self, script: str, *args: Any) -> Any:
        """Execute JavaScript."""
        return self.driver.execute_script(script, *args)

    def get_page_source(self) -> str:
        """Get the full page source HTML."""
        return self.driver.page_source

    def is_loading(self) -> bool:
        """Check if a loading spinner is visible."""
        loaders = [
            (By.CSS_SELECTOR, ".loading, .spinner, [class*='loading'], [class*='spinner']"),
            (By.XPATH, "//*[contains(@class,'loading')]"),
        ]
        for locator in loaders:
            try:
                el = self.driver.find_element(*locator)
                if el.is_displayed():
                    return True
            except NoSuchElementException:
                continue
        return False

    def wait_until_not_loading(self, timeout: int | None = None) -> None:
        """Wait until the loading spinner disappears."""
        wait = WebDriverWait(self.driver, timeout or self.timeout)
        try:
            wait.until(EC.invisibility_of_element_located(
                (By.CSS_SELECTOR, ".loading, .spinner, [class*='loading'], [class*='spinner']")
            ))
        except TimeoutException:
            pass

    def close(self) -> None:
        """Close the current browser tab/window."""
        self.driver.close()
