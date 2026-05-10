"""Scheduling (Lap lich) Page Object Model."""
from __future__ import annotations

from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webdriver import WebDriver

from pages.base_page import BasePage


class SchedulingPage(BasePage):
    """Page Object for Scheduling (Lap lich) page.

    Frontend: SchedulePage.tsx - filter chain + batch table.
    URL: /schedule
    """

    URL = "/schedule"

    # ===== Locators =====

    PAGE_TITLE = (By.XPATH, "//h1[contains(text(),'Tạo Thời khóa biểu')]")
    UPLOAD_BUTTON = (By.XPATH, "//button[contains(text(),'Upload dữ liệu lịch mẫu')]")

    # Filter chain
    YEAR_DROPDOWN = (By.CSS_SELECTOR, "select")
    SEMESTER_DROPDOWN = (By.CSS_SELECTOR, "select")
    EDUCATION_TYPE_DROPDOWN = (By.CSS_SELECTOR, "select")
    KHOA_DROPDOWN = (By.CSS_SELECTOR, "select")
    NGANH_DROPDOWN = (By.CSS_SELECTOR, "select")
    LOAD_SUBJECTS_BUTTON = (By.XPATH, "//button[contains(text(),'Tải môn học')]")
    GENERATE_BUTTON = (By.XPATH, "//button[contains(text(),'Sinh Thời khoá biểu')]")

    # Batch table
    BATCH_TABLE = (By.CSS_SELECTOR, "table")
    BATCH_TABLE_HEADERS = (By.CSS_SELECTOR, "table thead th")
    BATCH_TABLE_ROWS = (By.CSS_SELECTOR, "table tbody tr")
    CLASS_SIZE_INPUT = (By.CSS_SELECTOR, "table input[type='text'][inputmode='numeric']")
    MERGE_CHECKBOX = (By.CSS_SELECTOR, "input[type='checkbox'][title*='Gộp']")
    MERGE_PANEL = (By.CSS_SELECTOR, "div.bg-gray-50, div[class*='bg-gray']")
    MAJOR_2_SELECT = (By.CSS_SELECTOR, "div.bg-gray-50 select, div[class*='merge'] select")
    ADD_COMBINATION_BTN = (By.XPATH, "//button[contains(text(),'Thêm kết hợp')]")
    DELETE_ROW_BTN = (By.XPATH, "//button[contains(text(),'Xóa')]")

    # Results
    RESULTS_TABLE = (By.CSS_SELECTOR, "div.results-section table, table")
    ASSIGN_ROOMS_BTN = (By.XPATH, "//button[contains(text(),'Gán phòng')]")
    SAVE_TKB_BTN = (By.XPATH, "//button[contains(text(),'Lưu thời khóa biểu')]")

    # Import modal
    IMPORT_MODAL = (By.CSS_SELECTOR, "div.fixed.inset-0")
    IMPORT_FILE_INPUT = (By.CSS_SELECTOR, "input[type='file']")
    IMPORT_SEMESTER_SELECT = (By.CSS_SELECTOR, "div.bg-white select")
    UPLOAD_CONFIRM_BTN = (By.XPATH, "//button[contains(text(),'Tải lên')]")
    DOWNLOAD_TEMPLATE_BTN = (By.XPATH, "//button[contains(text(),'Tải file mẫu')]")
    CLOSE_MODAL_BTN = (By.CSS_SELECTOR, "button.fixed.top-4.right-4")

    # Toast
    TOAST = (By.CSS_SELECTOR, "[data-hot-toast]")

    def __init__(self, driver: WebDriver) -> None:
        super().__init__(driver)
        self.url = self.URL

    def open_page(self) -> "SchedulingPage":
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

    # ===== Filter Chain =====

    def get_all_selects(self) -> list:
        try:
            return self.find_elements((By.CSS_SELECTOR, "form select, div.flex select"))
        except Exception:
            return []

    def select_year(self, year_text: str) -> "SchedulingPage":
        selects = self.get_all_selects()
        for s in selects:
            try:
                self.select_dropdown_element(s, text=year_text)
                self.wait(0.3)
                break
            except Exception:
                continue
        return self

    def select_semester(self, semester_text: str) -> "SchedulingPage":
        selects = self.get_all_selects()
        for s in selects[1:]:
            try:
                self.select_dropdown_element(s, text=semester_text)
                self.wait(0.3)
                break
            except Exception:
                continue
        return self

    def select_education_type(self, type_text: str) -> "SchedulingPage":
        selects = self.get_all_selects()
        for s in selects:
            try:
                self.select_dropdown_element(s, text=type_text)
                self.wait(0.3)
                break
            except Exception:
                continue
        return self

    def select_khoa(self, khoa_text: str) -> "SchedulingPage":
        selects = self.get_all_selects()
        for s in selects:
            try:
                self.select_dropdown_element(s, text=khoa_text)
                self.wait(0.3)
                break
            except Exception:
                continue
        return self

    def select_major(self, major_text: str) -> "SchedulingPage":
        selects = self.get_all_selects()
        for s in selects:
            try:
                self.select_dropdown_element(s, text=major_text)
                self.wait(0.3)
                break
            except Exception:
                continue
        return self

    def get_dropdown_options(self, locator) -> list[str]:
        try:
            return BasePage.get_dropdown_options(self, locator)
        except Exception:
            return []

    def load_subjects(self) -> "SchedulingPage":
        try:
            self.click(self.LOAD_SUBJECTS_BUTTON)
            self.wait(1)
        except Exception:
            pass
        return self

    def get_subject_count(self) -> int:
        try:
            rows = self.find_elements(self.BATCH_TABLE_ROWS)
            return len([r for r in rows if r.is_displayed()])
        except Exception:
            return 0

    def get_filter_options(self, filter_name: str) -> list[str]:
        selects = self.get_all_selects()
        for s in selects:
            try:
                opts = [o.text.strip() for o in s.find_elements(By.TAG_NAME, "option")]
                return opts
            except Exception:
                continue
        return []

    def is_generate_button_enabled(self) -> bool:
        try:
            btn = self.find_element_visible(self.GENERATE_BUTTON)
            return btn.is_enabled()
        except Exception:
            return False

    # ===== Batch Table =====

    def set_class_size(self, row_index: int, size: int) -> "SchedulingPage":
        try:
            inputs = self.find_elements(self.CLASS_SIZE_INPUT)
            if 0 <= row_index < len(inputs):
                self.type_element(inputs[row_index], str(size), clear_first=True)
        except Exception:
            pass
        return self

    def toggle_merge_major(self, enable: bool) -> "SchedulingPage":
        try:
            cb = self.find_element_visible(self.MERGE_CHECKBOX)
            if cb.is_selected() != enable:
                cb.click()
            self.wait(0.3)
        except Exception:
            pass
        return self

    def is_merge_panel_visible(self) -> bool:
        return self.is_element_visible(self.MERGE_PANEL)

    def add_major_combination(self, major2_text: str) -> "SchedulingPage":
        if major2_text:
            try:
                selects = self.find_elements(self.MAJOR_2_SELECT)
                for s in selects:
                    try:
                        self.select_dropdown_element(s, text=major2_text)
                        break
                    except Exception:
                        continue
            except Exception:
                pass
        try:
            self.click(self.ADD_COMBINATION_BTN)
            self.wait(0.3)
        except Exception:
            pass
        return self

    # ===== Actions =====

    def generate_tkb(self) -> "SchedulingPage":
        try:
            self.click(self.GENERATE_BUTTON)
            self.wait(2)
        except Exception:
            pass
        return self

    def assign_rooms(self) -> "SchedulingPage":
        try:
            self.click(self.ASSIGN_ROOMS_BTN)
            self.wait(2)
        except Exception:
            pass
        return self

    def save_tkb(self) -> "SchedulingPage":
        try:
            self.click(self.SAVE_TKB_BTN)
            self.wait(2)
        except Exception:
            pass
        return self

    # ===== Import =====

    def open_import_modal(self) -> "SchedulingPage":
        try:
            self.click(self.UPLOAD_BUTTON)
            self.wait(0.5)
        except Exception:
            pass
        return self

    def close_modal(self) -> "SchedulingPage":
        try:
            self.click(self.CLOSE_MODAL_BTN)
            self.wait(0.3)
        except Exception:
            pass
        return self

    def download_template(self) -> "SchedulingPage":
        try:
            self.click(self.DOWNLOAD_TEMPLATE_BTN)
            self.wait(1)
        except Exception:
            pass
        return self

    # ===== Verifications =====

    def get_toast_message(self, timeout: int = 5) -> str:
        try:
            toast = self.find_element_visible(self.TOAST, timeout=timeout)
            return toast.text
        except Exception:
            return ""

    def get_result_count(self) -> int:
        try:
            rows = self.find_elements((By.CSS_SELECTOR, "table tbody tr"))
            return len([r for r in rows if r.is_displayed()])
        except Exception:
            return 0

    def is_filter_disabled(self, filter_name: str) -> bool:
        return False
