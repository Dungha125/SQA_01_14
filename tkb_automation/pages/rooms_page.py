"""Room Management Page Object Model."""
from __future__ import annotations

import re
import time
from typing import Optional

from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webdriver import WebDriver
from selenium.webdriver.remote.webelement import WebElement

from pages.base_page import BasePage


class RoomsPage(BasePage):
    """Page Object for Room Management (Quan ly phong hoc) page.

    Frontend: RoomsPage.tsx - React with tabs (Danh sach / Trang thai).
    No id attributes. Modal overlay: fixed inset-0 bg-black bg-opacity-50.
    """

    URL = "/rooms"

    # ===== Locators =====

    # Page structure
    PAGE_TITLE = (By.XPATH, "//h1[contains(text(),'Quản lý Phòng học')]")
    ADD_ROOM_BUTTON = (By.XPATH, "//button[.//span[contains(text(),'Thêm phòng học')]]")

    # Tabs
    TAB_LIST = (By.XPATH, "//button[contains(text(),'Danh sách phòng học')]")
    TAB_STATUS = (By.XPATH, "//button[contains(text(),'Trạng thái theo kì')]")

    # Tab 1 - Room list
    SEARCH_INPUT = (By.CSS_SELECTOR, "input[placeholder='Tìm kiếm phòng học...']")
    BUILDING_FILTER = (By.CSS_SELECTOR, "select")
    BUILDING_DROPDOWN_ALT = (By.CSS_SELECTOR, "div.flex select, form select")
    CAPACITY_MIN = (By.CSS_SELECTOR, "input[placeholder='Sức chứa tối thiểu']")
    CAPACITY_MAX = (By.CSS_SELECTOR, "input[placeholder='Sức chứa tối đa']")
    CLEAR_FILTERS = (By.XPATH, "//button[contains(text(),'Xóa tất cả')]")

    # Tab 2 - Status by semester
    SEMESTER_SELECT = (By.CSS_SELECTOR, "select")
    SEARCH_STATUS_TAB = (By.CSS_SELECTOR, "input[placeholder='Tìm kiếm theo mã phòng...']")

    # Table
    TABLE = (By.CSS_SELECTOR, "table")
    TABLE_HEADERS = (By.CSS_SELECTOR, "table thead th")
    TABLE_ROWS = (By.CSS_SELECTOR, "table tbody tr")
    PAGINATION_PREV = (By.CSS_SELECTOR, "button[class*='ChevronLeft'], button:has(svg)")  # first prev
    PAGINATION_NEXT = (By.CSS_SELECTOR, "button:last-child[class*='pagination'], button:has(svg)")  # last next

    # Row actions
    EDIT_BUTTON = (By.CSS_SELECTOR, "button:has(svg[class*='Edit'])")
    DELETE_BUTTON = (By.CSS_SELECTOR, "button:has(svg[class*='Trash2'])")

    # Modal overlay + form
    MODAL_OVERLAY = (By.CSS_SELECTOR, "div.fixed.inset-0.bg-black.bg-opacity-50")
    MODAL = (By.CSS_SELECTOR, "div.fixed.inset-0")
    MODAL_CLOSE = (By.CSS_SELECTOR, "button.fixed.top-4.right-4, button[aria-label='Close']")
    MODAL_FORM = (By.CSS_SELECTOR, "form, div.bg-white.rounded-lg")
    ROOM_CODE_INPUT = (By.CSS_SELECTOR, "input[type='text']")
    BUILDING_SELECT = (By.CSS_SELECTOR, "div.bg-white select, form select")
    CAPACITY_INPUT = (By.CSS_SELECTOR, "input[type='number']")
    FLOOR_INPUT = (By.CSS_SELECTOR, "input[placeholder='Tầng'], input[type='number']")
    ROOM_TYPE_SELECT = (By.CSS_SELECTOR, "select")
    SUBMIT_ADD = (By.XPATH, "//button[contains(text(),'Tạo mới')]")
    SUBMIT_EDIT = (By.XPATH, "//button[contains(text(),'Cập nhật')]")
    CANCEL_BUTTON = (By.XPATH, "//button[contains(text(),'Hủy')]")

    # Confirm dialog
    CONFIRM_OVERLAY = (By.CSS_SELECTOR, "div.fixed.inset-0.bg-black")
    CONFIRM_DELETE_BTN = (By.XPATH, "//button[contains(text(),'Xóa') and not(contains(text(),'Hủy'))]")
    CANCEL_DELETE_BTN = (By.XPATH, "//button[contains(text(),'Hủy')]")

    # Toast
    TOAST = (By.CSS_SELECTOR, "[data-hot-toast], [class*='toast']")

    def __init__(self, driver: WebDriver) -> None:
        super().__init__(driver)
        self.url = self.URL

    def open_page(self) -> "RoomsPage":
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

    def is_add_button_visible(self) -> bool:
        return self.is_element_visible(self.ADD_ROOM_BUTTON)

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

    def get_pagination_values(self) -> tuple[int, int]:
        return (0, 0)

    # ===== Tabs =====

    def switch_to_list_tab(self) -> "RoomsPage":
        """Switch to 'Danh sách phòng học' tab."""
        try:
            self.click(self.TAB_LIST)
            self.wait(0.5)
        except Exception:
            pass
        return self

    def switch_to_status_tab(self) -> "RoomsPage":
        """Switch to 'Trạng thái theo kì' tab."""
        try:
            self.click(self.TAB_STATUS)
            self.wait(0.5)
        except Exception:
            pass
        return self

    # ===== Search & Filter =====

    def search_room(self, keyword: str) -> "RoomsPage":
        self.type(self.SEARCH_INPUT, keyword, clear_first=True)
        self.wait(0.5)
        return self

    def filter_by_building(self, building: str) -> "RoomsPage":
        if building in ("-- Tất cả --", "Tất cả tòa nhà", "Tất cả"):
            building = "Tất cả tòa nhà"
        try:
            self.select_dropdown(self.BUILDING_FILTER, text=building)
            self.wait(0.5)
        except Exception:
            pass
        return self

    def filter_by_capacity_range(self, min_cap: int | None = None,
                                  max_cap: int | None = None) -> "RoomsPage":
        if min_cap is not None:
            try:
                self.type(self.CAPACITY_MIN, str(min_cap), clear_first=True)
            except Exception:
                pass
        if max_cap is not None:
            try:
                self.type(self.CAPACITY_MAX, str(max_cap), clear_first=True)
            except Exception:
                pass
        self.wait(0.5)
        return self

    def clear_all_filters(self) -> "RoomsPage":
        try:
            self.click(self.CLEAR_FILTERS)
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

    def click_add_room(self) -> "RoomsPage":
        self.click(self.ADD_ROOM_BUTTON)
        self.wait_for_modal()
        return self

    def fill_room_form(self, room_code: str, building: str, capacity: int,
                       floor: int | None = None,
                       room_type: str = "Phòng thường") -> "RoomsPage":
        try:
            self.type(self.ROOM_CODE_INPUT, room_code, clear_first=True)
        except Exception:
            pass
        try:
            selects = self.find_elements(self.BUILDING_SELECT)
            if selects:
                self.select_dropdown_element(selects[0], text=building)
        except Exception:
            pass
        try:
            nums = self.find_elements(self.CAPACITY_INPUT)
            if nums:
                self.type_element(nums[0], str(capacity), clear_first=True)
        except Exception:
            pass
        if floor is not None:
            try:
                all_nums = self.find_elements(self.FLOOR_INPUT)
                for inp in all_nums:
                    try:
                        self.type_element(inp, str(floor), clear_first=True)
                        break
                    except Exception:
                        continue
            except Exception:
                pass
        try:
            selects = self.find_elements(self.ROOM_TYPE_SELECT)
            if selects:
                self.select_dropdown_element(selects[-1], text=room_type)
        except Exception:
            pass
        return self

    def submit_form(self, wait_result: bool = True) -> "RoomsPage":
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

    def cancel_form(self) -> "RoomsPage":
        try:
            self.click(self.CANCEL_BUTTON)
        except Exception:
            try:
                self.click(self.MODAL_CLOSE)
            except Exception:
                pass
        self.wait(0.3)
        return self

    def add_room(self, room_code: str, building: str, capacity: int,
                 floor: int | None = None,
                 room_type: str = "Phòng thường") -> "RoomsPage":
        self.click_add_room()
        self.fill_room_form(room_code, building, capacity, floor, room_type)
        self.submit_form()
        return self

    # ===== Row Operations =====

    def _find_room_row(self, room_code: str) -> WebElement | None:
        try:
            rows = self.find_elements(self.TABLE_ROWS)
            for row in rows:
                if room_code in row.text:
                    return row
        except Exception:
            pass
        return None

    def edit_room(self, room_code: str) -> "RoomsPage":
        row = self._find_room_row(room_code)
        if row:
            try:
                edit = row.find_element(By.CSS_SELECTOR, "button:has(svg[class*='Edit'])")
                edit.click()
                self.wait_for_modal()
            except Exception:
                pass
        return self

    def delete_room(self, room_code: str, confirm: bool = True) -> "RoomsPage":
        row = self._find_room_row(room_code)
        if row:
            try:
                delete = row.find_element(By.CSS_SELECTOR, "button:has(svg[class*='Trash2'])")
                delete.click()
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

    def is_room_in_table(self, room_code: str) -> bool:
        return self._find_room_row(room_code) is not None

    def get_room_info(self, room_code: str) -> dict[str, str]:
        row = self._find_room_row(room_code)
        if not row:
            return {}
        cells = row.find_elements(By.TAG_NAME, "td")
        headers = self.get_table_headers()
        return {headers[i]: cells[i].text.strip()
                 for i in range(min(len(cells), len(headers)))}

    def get_toast_message(self, timeout: int = 5) -> str:
        try:
            toast = self.find_element_visible(self.TOAST, timeout=timeout)
            return toast.text
        except Exception:
            return ""

    def is_filter_disabled(self, filter_name: str) -> bool:
        return False

    # ===== Pagination =====

    def go_to_page(self, page_num: int) -> "RoomsPage":
        try:
            btns = self.find_elements(
                (By.CSS_SELECTOR, "button[class*='pagination'] button, button:has-text('1')")
            )
            for btn in btns:
                if str(page_num) in btn.text:
                    btn.click()
                    self.wait(0.5)
                    break
        except Exception:
            pass
        return self

    def click_next(self) -> "RoomsPage":
        try:
            btns = self.find_elements((By.CSS_SELECTOR, "button"))
            for btn in btns:
                svg = btn.find_elements(By.CSS_SELECTOR, "svg[class*='ChevronRight']")
                if svg:
                    btn.click()
                    self.wait(0.5)
                    break
        except Exception:
            pass
        return self
