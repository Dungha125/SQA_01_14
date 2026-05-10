"""Semester Management Page Object Model."""
from __future__ import annotations

from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webdriver import WebDriver

from pages.base_page import BasePage


class SemestersPage(BasePage):
    """Page Object for Semester Management (Quan ly hoc ki) page.

    Frontend: SemestersPage.tsx.
    Has stat cards (Total/Active/Inactive), table, modal with DatePickerInput.
    """

    URL = "/semesters"

    # ===== Locators =====

    PAGE_TITLE = (By.XPATH, "//h1[contains(text(),'Quản lý học kỳ')]")
    ADD_BUTTON = (By.XPATH, "//button[.//span[contains(text(),'Thêm học kỳ')]]")

    # Stat cards
    STAT_TOTAL = (By.XPATH, "//div[contains(@class,'border-l-4')][.//span[contains(text(),'Tổng')]]")
    STAT_ACTIVE = (By.XPATH, "//div[contains(@class,'border-l-4')][.//span[contains(text(),'Đang hoạt động')]]")
    STAT_INACTIVE = (By.XPATH, "//div[contains(@class,'border-l-4')][.//span[contains(text(),'Vô hiệu hóa')]]")

    # Table
    TABLE = (By.CSS_SELECTOR, "table")
    TABLE_HEADERS = (By.CSS_SELECTOR, "table thead th")
    TABLE_ROWS = (By.CSS_SELECTOR, "table tbody tr")

    # Row actions
    ACTIVATE_BUTTON = (By.CSS_SELECTOR, "button[title='Kích hoạt học kỳ']")
    EDIT_BUTTON = (By.CSS_SELECTOR, "button:has(svg[class*='Edit'])")
    DELETE_BUTTON = (By.CSS_SELECTOR, "button:has(svg[class*='Trash2'])")

    # Modal
    MODAL_OVERLAY = (By.CSS_SELECTOR, "div.fixed.inset-0.bg-black")
    MODAL = (By.CSS_SELECTOR, "div.fixed.inset-0")
    MODAL_CLOSE = (By.CSS_SELECTOR, "button.fixed.top-4.right-4")

    # Form fields (no id - use input types and surrounding text)
    NAME_INPUT = (By.CSS_SELECTOR, "input[type='text']")
    YEAR_INPUT = (By.CSS_SELECTOR, "input[placeholder*='202']")
    DESCRIPTION_INPUT = (By.CSS_SELECTOR, "textarea")
    DATE_INPUTS = (By.CSS_SELECTOR, "input[placeholder*='/'], input[placeholder*='Ngày']")
    ACTIVE_CHECKBOX = (By.CSS_SELECTOR, "input[type='checkbox']")

    SUBMIT_ADD = (By.XPATH, "//button[contains(text(),'Thêm')]")
    SUBMIT_EDIT = (By.XPATH, "//button[contains(text(),'Cập nhật')]")
    CANCEL_BUTTON = (By.XPATH, "//button[contains(text(),'Hủy')]")

    # Confirm dialog
    CONFIRM_DELETE_BTN = (By.XPATH, "//button[contains(text(),'Xác nhận xóa')]")
    CANCEL_DELETE_BTN = (By.XPATH, "//button[contains(text(),'Hủy')]")

    # Toast
    TOAST = (By.CSS_SELECTOR, "[data-hot-toast]")

    def __init__(self, driver: WebDriver) -> None:
        super().__init__(driver)
        self.url = self.URL

    def open_page(self) -> "SemestersPage":
        from core.config_loader import config
        self.driver.get(f"{config.app_url}{self.URL}")
        self.wait_for_load()
        return self

    def wait_for_load(self, timeout: int = 10) -> None:
        try:
            self.wait_until_not_loading(timeout)
            self.wait(0.5)
        except Exception:
            pass

    # ===== UI Verification =====

    def get_title(self) -> str:
        try:
            return self.get_text(self.PAGE_TITLE)
        except Exception:
            return ""

    def get_table_headers(self) -> list[str]:
        try:
            headers = self.find_elements(self.TABLE_HEADERS)
            return [h.text.strip() for h in headers if h.text.strip()]
        except Exception:
            return []

    def get_row_count(self) -> int:
        try:
            rows = self.find_elements(self.TABLE_ROWS)
            return len([r for r in rows if r.is_displayed()])
        except Exception:
            return 0

    def get_pagination_label(self) -> str:
        return ""

    # ===== Modal =====

    def is_modal_open(self) -> bool:
        return self.is_element_visible(self.MODAL_OVERLAY)

    def wait_for_modal(self, timeout: int = 5) -> None:
        try:
            self.find_element_visible(self.MODAL_OVERLAY, timeout=timeout)
            self.wait(0.3)
        except Exception:
            pass

    def click_add_semester(self) -> "SemestersPage":
        self.click(self.ADD_BUTTON)
        self.wait_for_modal()
        return self

    def fill_semester_form(self, name: str, year: str,
                          start_date: str = "", end_date: str = "",
                          description: str = "") -> "SemestersPage":
        try:
            inputs = self.find_elements(self.NAME_INPUT)
            if inputs:
                self.type_element(inputs[0], name, clear_first=True)
        except Exception:
            pass
        try:
            inputs = self.find_elements(self.YEAR_INPUT)
            if inputs:
                self.type_element(inputs[0], year, clear_first=True)
        except Exception:
            pass
        try:
            date_inputs = self.find_elements(self.DATE_INPUTS)
            if start_date and date_inputs:
                self.type_element(date_inputs[0], start_date, clear_first=True)
            if end_date and len(date_inputs) > 1:
                self.type_element(date_inputs[1], end_date, clear_first=True)
        except Exception:
            pass
        if description:
            try:
                self.type(self.DESCRIPTION_INPUT, description, clear_first=True)
            except Exception:
                pass
        return self

    def check_active(self) -> "SemestersPage":
        try:
            cb = self.find_element_visible(self.ACTIVE_CHECKBOX)
            if not cb.is_selected():
                cb.click()
        except Exception:
            pass
        return self

    def uncheck_active(self) -> "SemestersPage":
        try:
            cb = self.find_element_visible(self.ACTIVE_CHECKBOX)
            if cb.is_selected():
                cb.click()
        except Exception:
            pass
        return self

    def submit_form(self, wait_result: bool = True) -> "SemestersPage":
        try:
            self.click(self.SUBMIT_ADD)
        except Exception:
            try:
                self.click(self.SUBMIT_EDIT)
            except Exception:
                pass
        if wait_result:
            self.wait(0.5)
        return self

    def cancel_form(self) -> "SemestersPage":
        try:
            self.click(self.CANCEL_BUTTON)
        except Exception:
            try:
                self.click(self.MODAL_CLOSE)
            except Exception:
                pass
        self.wait(0.3)
        return self

    def add_semester(self, name: str, year: str,
                     start_date: str = "", end_date: str = "",
                     description: str = "", is_active: bool = False) -> "SemestersPage":
        self.click_add_semester()
        self.fill_semester_form(name, year, start_date, end_date, description)
        if is_active:
            self.check_active()
        self.submit_form()
        return self

    # ===== Row Operations =====

    def _find_semester_row(self, semester_name: str) -> object | None:
        try:
            rows = self.find_elements(self.TABLE_ROWS)
            for row in rows:
                if semester_name in row.text:
                    return row
        except Exception:
            pass
        return None

    def edit_semester(self, semester_name: str) -> "SemestersPage":
        row = self._find_semester_row(semester_name)
        if row:
            try:
                edit = row.find_element(By.CSS_SELECTOR, "button:has(svg[class*='Edit'])")
                edit.click()
                self.wait_for_modal()
            except Exception:
                pass
        return self

    def activate_semester(self, semester_name: str) -> "SemestersPage":
        row = self._find_semester_row(semester_name)
        if row:
            try:
                btn = row.find_element(By.CSS_SELECTOR, "button[title='Kích hoạt học kỳ']")
                btn.click()
                self.wait(0.5)
            except Exception:
                pass
        return self

    def delete_semester(self, semester_name: str = "", confirm: bool = True) -> "SemestersPage":
        row = self._find_semester_row(semester_name) if semester_name else None
        if row:
            try:
                btn = row.find_element(By.CSS_SELECTOR, "button:has(svg[class*='Trash2'])")
                btn.click()
                self.wait(0.5)
                if confirm:
                    try:
                        self.click(self.CONFIRM_DELETE_BTN)
                    except Exception:
                        self.accept_alert()
                else:
                    try:
                        self.click(self.CANCEL_DELETE_BTN)
                    except Exception:
                        self.dismiss_alert()
            except Exception:
                pass
        self.wait(0.5)
        return self

    # ===== Verifications =====

    def is_semester_in_table(self, semester_name: str) -> bool:
        return self._find_semester_row(semester_name) is not None

    def get_form_error(self) -> str:
        return ""

    def get_toast_message(self, timeout: int = 5) -> str:
        try:
            toast = self.find_element_visible(self.TOAST, timeout=timeout)
            return toast.text
        except Exception:
            return ""
