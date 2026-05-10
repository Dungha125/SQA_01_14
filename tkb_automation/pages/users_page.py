"""User Management Page Object Model."""
from __future__ import annotations

from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webdriver import WebDriver

from pages.base_page import BasePage


class UsersPage(BasePage):
    """Page Object for User Management (Quan ly nguoi dung) page.

    Frontend: UsersPage.tsx.
    URL: /users
    Has tab filters (Tat ca / Dang hoat dong / Vo hieu hoa).
    Toggle switch: inline-flex h-6 w-11 items-center rounded-full.
    """

    URL = "/users"

    # ===== Locators =====

    PAGE_TITLE = (By.XPATH, "//h1[contains(text(),'Quản lý người dùng')]")

    # Tab filters
    TAB_ALL = (By.XPATH, "//button[contains(text(),'Tất cả')]")
    TAB_ACTIVE = (By.XPATH, "//button[contains(text(),'Đang hoạt động')]")
    TAB_INACTIVE = (By.XPATH, "//button[contains(text(),'Bị vô hiệu hóa')]")

    # Table
    TABLE = (By.CSS_SELECTOR, "table")
    TABLE_HEADERS = (By.CSS_SELECTOR, "table thead th")
    TABLE_ROWS = (By.CSS_SELECTOR, "table tbody tr")

    # Row actions - toggle switch
    TOGGLE_SWITCH = (By.CSS_SELECTOR, "button.inline-flex.h-6.w-11.items-center.rounded-full")
    DELETE_BUTTON = (By.CSS_SELECTOR, "button:has(svg[class*='Trash2'])")

    # Confirm dialog
    CONFIRM_DELETE_BTN = (By.XPATH, "//button[contains(text(),'Xóa') and not(contains(text(),'Hủy'))]")
    CANCEL_DELETE_BTN = (By.XPATH, "//button[contains(text(),'Hủy')]")

    # Toast
    TOAST = (By.CSS_SELECTOR, "[data-hot-toast]")

    def __init__(self, driver: WebDriver) -> None:
        super().__init__(driver)
        self.url = self.URL

    def open_page(self) -> "UsersPage":
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

    # ===== Tabs =====

    def click_tab_all(self) -> "UsersPage":
        try:
            self.click(self.TAB_ALL)
            self.wait(0.5)
        except Exception:
            pass
        return self

    def click_tab_active(self) -> "UsersPage":
        try:
            self.click(self.TAB_ACTIVE)
            self.wait(0.5)
        except Exception:
            pass
        return self

    def click_tab_inactive(self) -> "UsersPage":
        try:
            self.click(self.TAB_INACTIVE)
            self.wait(0.5)
        except Exception:
            pass
        return self

    def get_tab_count(self, tab: str) -> int:
        try:
            if tab == "all":
                tab_el = self.TAB_ALL
            elif tab == "active":
                tab_el = self.TAB_ACTIVE
            else:
                tab_el = self.TAB_INACTIVE
            text = self.get_text(tab_el)
            import re
            m = re.search(r"\((\d+)\)", text)
            return int(m.group(1)) if m else 0
        except Exception:
            return 0

    # ===== Row Operations =====

    def _find_user_row(self, username: str = "") -> object | None:
        try:
            rows = self.find_elements(self.TABLE_ROWS)
            for row in rows:
                if not username or username in row.text:
                    return row
        except Exception:
            pass
        return None

    def _click_toggle_in_row(self, row, activate: bool) -> None:
        try:
            toggle = row.find_element(By.CSS_SELECTOR, "button.inline-flex.h-6.w-11")
            toggle.click()
            self.wait(0.5)
        except Exception:
            pass

    def activate_user(self, username: str = "", confirm: bool = True) -> "UsersPage":
        row = self._find_user_row(username) if username else None
        if row:
            self._click_toggle_in_row(row, activate=True)
        return self

    def deactivate_user(self, username: str = "", confirm: bool = True) -> "UsersPage":
        row = self._find_user_row(username) if username else None
        if row:
            self._click_toggle_in_row(row, activate=False)
        return self

    def delete_user(self, username: str = "", confirm: bool = True) -> "UsersPage":
        row = self._find_user_row(username) if username else None
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

    def get_toast_message(self, timeout: int = 5) -> str:
        try:
            toast = self.find_element_visible(self.TOAST, timeout=timeout)
            return toast.text
        except Exception:
            return ""
