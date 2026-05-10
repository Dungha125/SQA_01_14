"""Timetable Management (Saved Schedules / QL TKB) Page Object Model."""
from __future__ import annotations

from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webdriver import WebDriver

from pages.base_page import BasePage


class TKBPage(BasePage):
    """Page Object for Saved Schedules / Timetable Management page.

    Frontend: SavedSchedulesPage.tsx.
    URL: /saved-schedules
    Has export button, filter dropdowns, delete by major/all modals.
    """

    URL = "/saved-schedules"

    # ===== Locators =====

    PAGE_TITLE = (By.XPATH, "//h1[contains(text(),'Quản lý Thời khóa biểu')] | //h2[contains(text(),'Thời Khóa Biểu')]")
    EXPORT_EXCEL_BUTTON = (By.XPATH, "//button[contains(text(),'Xuất Excel')]")

    # Filters
    YEAR_FILTER = (By.CSS_SELECTOR, "select")
    SEMESTER_FILTER = (By.CSS_SELECTOR, "select")
    KHOA_FILTER = (By.CSS_SELECTOR, "select")
    MAJOR_FILTER = (By.CSS_SELECTOR, "select")

    # Actions
    DELETE_MAJOR_BUTTON = (By.XPATH, "//button[contains(text(),'Xóa ngành')]")
    DELETE_ALL_BUTTON = (By.XPATH, "//button[contains(text(),'Xóa Tất Cả')]")

    # Total counter
    TOTAL_COUNT = (By.CSS_SELECTOR, "span:has-text('Tổng:')")

    # Table
    TABLE = (By.CSS_SELECTOR, "table")
    TABLE_HEADERS = (By.CSS_SELECTOR, "table thead th, table thead div")
    TABLE_ROWS = (By.CSS_SELECTOR, "table tbody tr")

    # Toast
    TOAST = (By.CSS_SELECTOR, "[data-hot-toast]")

    def __init__(self, driver: WebDriver) -> None:
        super().__init__(driver)
        self.url = self.URL

    def open_page(self) -> "TKBPage":
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

    def get_tkb_count(self) -> int:
        try:
            text = self.get_text(self.TOTAL_COUNT)
            import re
            m = re.search(r"(\d+)", text)
            return int(m.group(1)) if m else 0
        except Exception:
            return 0

    # ===== Filters =====

    def _get_all_selects(self) -> list:
        try:
            return self.find_elements((By.CSS_SELECTOR, "select"))
        except Exception:
            return []

    def get_filter_options(self, filter_name: str) -> list[str]:
        selects = self._get_all_selects()
        for s in selects:
            try:
                opts = [o.text.strip() for o in s.find_elements(By.TAG_NAME, "option")]
                return opts
            except Exception:
                continue
        return []

    def filter_by_year(self, year: str) -> "TKBPage":
        selects = self._get_all_selects()
        for s in selects:
            try:
                self.select_dropdown_element(s, text=year)
                self.wait(0.3)
                break
            except Exception:
                continue
        return self

    def filter_by_semester(self, semester: str) -> "TKBPage":
        selects = self._get_all_selects()
        for s in selects:
            try:
                self.select_dropdown_element(s, text=semester)
                self.wait(0.3)
                break
            except Exception:
                continue
        return self

    def filter_by_khoa(self, khoa: str) -> "TKBPage":
        selects = self._get_all_selects()
        for s in selects:
            try:
                self.select_dropdown_element(s, text=khoa)
                self.wait(0.3)
                break
            except Exception:
                continue
        return self

    def filter_by_major(self, major: str) -> "TKBPage":
        selects = self._get_all_selects()
        for s in selects:
            try:
                self.select_dropdown_element(s, text=major)
                self.wait(0.3)
                break
            except Exception:
                continue
        return self

    def apply_filters(self, **kwargs) -> "TKBPage":
        if "year" in kwargs:
            self.filter_by_year(kwargs["year"])
        if "semester" in kwargs:
            self.filter_by_semester(kwargs["semester"])
        if "khoa" in kwargs:
            self.filter_by_khoa(kwargs["khoa"])
        if "major" in kwargs:
            self.filter_by_major(kwargs["major"])
        return self

    def has_filter_options(self) -> bool:
        selects = self._get_all_selects()
        return len(selects) > 0

    # ===== Actions =====

    def is_export_enabled(self) -> bool:
        try:
            btn = self.find_element_visible(self.EXPORT_EXCEL_BUTTON)
            return btn.is_enabled()
        except Exception:
            return False

    def export_excel(self) -> "TKBPage":
        try:
            self.click(self.EXPORT_EXCEL_BUTTON)
            self.wait(2)
        except Exception:
            pass
        return self

    def delete_major(self, confirm: bool = True) -> "TKBPage":
        try:
            self.click(self.DELETE_MAJOR_BUTTON)
            self.wait(0.5)
            if confirm:
                confirm_btn = self.find_element_visible(
                    (By.XPATH, "//button[contains(text(),'Xóa')]"), timeout=3
                )
                confirm_btn.click()
            else:
                cancel_btn = self.find_element_visible(
                    (By.XPATH, "//button[contains(text(),'Hủy')]"), timeout=3
                )
                cancel_btn.click()
        except Exception:
            pass
        self.wait(0.5)
        return self

    def delete_all(self, confirm: bool = True) -> "TKBPage":
        try:
            self.click(self.DELETE_ALL_BUTTON)
            self.wait(0.5)
            if confirm:
                confirm_btn = self.find_element_visible(
                    (By.XPATH, "//button[contains(text(),'Xóa')]"), timeout=3
                )
                confirm_btn.click()
            else:
                cancel_btn = self.find_element_visible(
                    (By.XPATH, "//button[contains(text(),'Hủy')]"), timeout=3
                )
                cancel_btn.click()
        except Exception:
            pass
        self.wait(0.5)
        return self

    # ===== Verifications =====

    def get_toast_message(self, timeout: int = 5) -> str:
        try:
            toast = self.find_element_visible(self.TOAST, timeout=timeout)
            return toast.text
        except Exception:
            return ""
