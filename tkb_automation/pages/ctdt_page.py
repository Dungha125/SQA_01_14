"""Training Program (CTDT / Subjects) Page Object Model."""
from __future__ import annotations

from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webdriver import WebDriver

from pages.base_page import BasePage


class CTDTPage(BasePage):
    """Page Object for CTDT (Training Program / Subjects) page.

    Frontend: SubjectsPage.tsx.
    URL: /subjects
    """

    URL = "/subjects"

    # ===== Locators =====

    PAGE_TITLE = (By.XPATH, "//h1[contains(text(),'Quản lý Môn học')]")
    ADD_BUTTON = (By.XPATH, "//button[.//span[contains(text(),'Thêm môn học')]]")
    UPLOAD_BUTTON = (By.XPATH, "//button[contains(text(),'Upload môn học')]")

    # Filters
    SEARCH_INPUT = (By.CSS_SELECTOR, "input[placeholder*='Tìm theo mã']")
    YEAR_DROPDOWN = (By.CSS_SELECTOR, "select")
    SEMESTER_DROPDOWN = (By.CSS_SELECTOR, "select")
    KHOA_DROPDOWN = (By.CSS_SELECTOR, "select")
    MAJOR_DROPDOWN = (By.CSS_SELECTOR, "select")
    EDUCATION_TYPE_DROPDOWN = (By.CSS_SELECTOR, "select")
    CLEAR_FILTERS_BTN = (By.XPATH, "//button[contains(text(),'Xóa tất cả')]")

    # Bulk actions
    BULK_DELETE_COUNTER = (By.CSS_SELECTOR, "span:has-text('Đã chọn')")
    BULK_DELETE_BTN = (By.XPATH, "//button[contains(text(),'Xóa') and contains(text(),'đã chọn')]")

    # Table
    TABLE = (By.CSS_SELECTOR, "table")
    TABLE_HEADERS = (By.CSS_SELECTOR, "table thead th")
    TABLE_ROWS = (By.CSS_SELECTOR, "table tbody tr")
    ROW_CHECKBOX = (By.CSS_SELECTOR, "table input[type='checkbox']")
    VIEW_BUTTON = (By.CSS_SELECTOR, "button:has(svg[class*='Eye'])")
    EDIT_BUTTON = (By.CSS_SELECTOR, "button:has(svg[class*='Edit'])")
    DELETE_BUTTON = (By.CSS_SELECTOR, "button:has(svg[class*='Trash2'])")

    # Pagination
    PAGINATION_INFO = (By.CSS_SELECTOR, "div:has-text('Hiển thị')")

    # Modal
    MODAL_OVERLAY = (By.CSS_SELECTOR, "div.fixed.inset-0.bg-black")
    MODAL_CLOSE = (By.CSS_SELECTOR, "button.fixed.top-4.right-4")
    # Form inputs
    SUBJECT_CODE_INPUT = (By.CSS_SELECTOR, "input[placeholder*='Mã']")
    SUBJECT_NAME_INPUT = (By.CSS_SELECTOR, "input[placeholder*='Tên']")
    CREDITS_INPUT = (By.CSS_SELECTOR, "input[type='number']")
    CLASS_YEAR_INPUT = (By.CSS_SELECTOR, "input[placeholder*='Khóa']")
    STUDENT_COUNT_INPUT = (By.CSS_SELECTOR, "input[placeholder*='Số sinh']")
    CLASS_COUNT_INPUT = (By.CSS_SELECTOR, "input[placeholder*='Số lớp']")
    SEMESTER_SELECT = (By.CSS_SELECTOR, "div.bg-white select")
    FACULTY_SELECT = (By.CSS_SELECTOR, "div.bg-white select")
    COMMON_CHECKBOX = (By.CSS_SELECTOR, "input[type='checkbox']")
    ALL_FORM_INPUTS = (By.CSS_SELECTOR, "div.bg-white input, div.bg-white textarea, div.bg-white select")
    SUBMIT_ADD = (By.XPATH, "//button[contains(text(),'Thêm')]")
    SUBMIT_EDIT = (By.XPATH, "//button[contains(text(),'Cập nhật')]")
    CANCEL_BUTTON = (By.XPATH, "//button[contains(text(),'Hủy')]")

    # Confirm dialog
    CONFIRM_DELETE_BTN = (By.XPATH, "//button[contains(text(),'Xóa') and not(contains(text(),'Hủy'))]")
    CANCEL_DELETE_BTN = (By.XPATH, "//button[contains(text(),'Hủy')]")

    # Toast
    TOAST = (By.CSS_SELECTOR, "[data-hot-toast]")

    def __init__(self, driver: WebDriver) -> None:
        super().__init__(driver)
        self.url = self.URL

    def open_page(self) -> "CTDTPage":
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

    def get_pagination_label(self) -> str:
        try:
            return self.get_text(self.PAGINATION_INFO)
        except Exception:
            return ""

    # ===== Filters =====

    def search_subject(self, keyword: str) -> "CTDTPage":
        self.type(self.SEARCH_INPUT, keyword, clear_first=True)
        self.wait(0.5)
        return self

    def filter_by_year(self, year: str) -> "CTDTPage":
        try:
            selects = self.find_elements((By.CSS_SELECTOR, "div.flex select, form select"))
            for s in selects:
                self.select_dropdown_element(s, text=year)
                self.wait(0.3)
                break
        except Exception:
            pass
        return self

    def filter_by_semester(self, semester: str) -> "CTDTPage":
        try:
            selects = self.find_elements((By.CSS_SELECTOR, "div.flex select, form select"))
            for s in selects:
                self.select_dropdown_element(s, text=semester)
                self.wait(0.3)
                break
        except Exception:
            pass
        return self

    def filter_by_khoa(self, khoa: str) -> "CTDTPage":
        try:
            selects = self.find_elements((By.CSS_SELECTOR, "div.flex select, form select"))
            for s in selects:
                self.select_dropdown_element(s, text=khoa)
                self.wait(0.3)
                break
        except Exception:
            pass
        return self

    def filter_by_major(self, major: str) -> "CTDTPage":
        try:
            selects = self.find_elements((By.CSS_SELECTOR, "div.flex select, form select"))
            for s in selects:
                self.select_dropdown_element(s, text=major)
                self.wait(0.3)
                break
        except Exception:
            pass
        return self

    def filter_by_education_type(self, edu_type: str) -> "CTDTPage":
        try:
            selects = self.find_elements((By.CSS_SELECTOR, "div.flex select, form select"))
            for s in selects:
                self.select_dropdown_element(s, text=edu_type)
                self.wait(0.3)
                break
        except Exception:
            pass
        return self

    def clear_filters(self) -> "CTDTPage":
        try:
            self.click(self.CLEAR_FILTERS_BTN)
            self.wait(0.3)
        except Exception:
            pass
        return self

    # ===== Modal =====

    def is_modal_open(self) -> bool:
        return self.is_element_visible(self.MODAL_OVERLAY)

    def wait_for_modal(self, timeout: int = 5) -> None:
        try:
            self.find_element_visible(self.MODAL_OVERLAY, timeout=timeout)
            self.wait(0.3)
        except Exception:
            pass

    def click_add_subject(self) -> "CTDTPage":
        self.click(self.ADD_BUTTON)
        self.wait_for_modal()
        return self

    def fill_subject_form(self, code: str, name: str, credits: int,
                          student_count: int = 0) -> "CTDTPage":
        try:
            self.type(self.SUBJECT_CODE_INPUT, code, clear_first=True)
        except Exception:
            pass
        try:
            self.type(self.SUBJECT_NAME_INPUT, name, clear_first=True)
        except Exception:
            pass
        try:
            nums = self.find_elements(self.CREDITS_INPUT)
            if nums:
                self.type_element(nums[0], str(credits), clear_first=True)
        except Exception:
            pass
        return self

    def submit_form(self, wait_result: bool = True) -> "CTDTPage":
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

    def cancel_form(self) -> "CTDTPage":
        try:
            self.click(self.CANCEL_BUTTON)
        except Exception:
            try:
                self.click(self.MODAL_CLOSE)
            except Exception:
                pass
        self.wait(0.3)
        return self

    def add_subject(self, code: str, name: str, credits: int) -> "CTDTPage":
        self.click_add_subject()
        self.fill_subject_form(code, name, credits)
        self.submit_form()
        return self

    # ===== Row Operations =====

    def _find_subject_row(self, code_or_name: str) -> object | None:
        try:
            rows = self.find_elements(self.TABLE_ROWS)
            for row in rows:
                if code_or_name in row.text:
                    return row
        except Exception:
            pass
        return None

    def edit_subject(self, code_or_name: str) -> "CTDTPage":
        row = self._find_subject_row(code_or_name)
        if row:
            try:
                edit = row.find_element(By.CSS_SELECTOR, "button:has(svg[class*='Edit'])")
                edit.click()
                self.wait_for_modal()
            except Exception:
                pass
        return self

    def delete_subject(self, code_or_name: str = "", confirm: bool = True) -> "CTDTPage":
        row = self._find_subject_row(code_or_name) if code_or_name else None
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

    def is_subject_in_table(self, code_or_name: str) -> bool:
        return self._find_subject_row(code_or_name) is not None

    def get_form_error(self) -> str:
        return ""

    def get_toast_message(self, timeout: int = 5) -> str:
        try:
            toast = self.find_element_visible(self.TOAST, timeout=timeout)
            return toast.text
        except Exception:
            return ""
