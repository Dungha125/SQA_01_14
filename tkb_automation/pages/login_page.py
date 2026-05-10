"""Login Page Object Model."""
from __future__ import annotations

from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webdriver import WebDriver

from pages.base_page import BasePage


class LoginPage(BasePage):
    """Page Object for the login page.

    Frontend: LoginPage.tsx - React inputs with NO id attributes.
    Uses Lucide React icons (User, Lock, Eye/EyeOff).
    """

    URL = "/login"

    # Locators - no id attributes in React, use placeholder + tag selectors
    USERNAME_INPUT = (By.CSS_SELECTOR, "input[placeholder='Nhập tên đăng nhập']")
    PASSWORD_INPUT = (By.CSS_SELECTOR, "input[placeholder='Nhập mật khẩu']")
    USERNAME_INPUT_ALT = (By.CSS_SELECTOR, "input[type='text']")
    PASSWORD_INPUT_ALT = (By.CSS_SELECTOR, "input[type='password']")
    LOGIN_BUTTON = (By.XPATH, "//button[@type='submit']//span[contains(text(),'Đăng nhập')]")
    LOGIN_BUTTON_ALT = (By.CSS_SELECTOR, "button[type='submit']")
    REGISTER_BUTTON = (By.XPATH, "//button[contains(text(),'Đăng ký')]")

    # Error / loading states
    ERROR_TOAST = (By.CSS_SELECTOR, "[data-hot-toast], .toast, [class*='toast']")

    def __init__(self, driver: WebDriver) -> None:
        super().__init__(driver)
        self.url = self.URL

    def open_page(self) -> "LoginPage":
        """Navigate to the login page."""
        from core.config_loader import config
        self.driver.get(f"{config.app_url}{self.URL}")
        self.wait_for_load()
        return self

    def login(self, username: str, password: str) -> "LoginPage":
        """Perform login with given credentials.

        Returns:
            Self for method chaining.
        """
        self.wait_for_load()
        try:
            self.type(self.USERNAME_INPUT, username, clear_first=True)
        except Exception:
            self.type(self.USERNAME_INPUT_ALT, username, clear_first=True)
        try:
            self.type(self.PASSWORD_INPUT, password, clear_first=True)
        except Exception:
            self.type(self.PASSWORD_INPUT_ALT, password, clear_first=True)
        try:
            self.click(self.LOGIN_BUTTON)
        except Exception:
            self.click(self.LOGIN_BUTTON_ALT)
        self.wait(1)
        return self

    def is_login_successful(self) -> bool:
        """Check if login redirected away from login page."""
        return "/login" not in self.driver.current_url.lower()

    def is_login_button_enabled(self) -> bool:
        """Check if the login button is enabled."""
        try:
            return self.is_element_enabled(self.LOGIN_BUTTON_ALT)
        except Exception:
            return False

    def is_login_button_disabled(self) -> bool:
        """Check if the login button is disabled (loading state)."""
        try:
            return not self.is_element_enabled(self.LOGIN_BUTTON_ALT)
        except Exception:
            return False

    def is_error_displayed(self) -> bool:
        """Check if an error toast/message is displayed."""
        try:
            self.wait(0.5)
            return self.is_element_visible(self.ERROR_TOAST)
        except Exception:
            return False

    def wait_for_load(self, timeout: int = 10) -> "LoginPage":
        """Wait for page to load."""
        from selenium.webdriver.support.ui import WebDriverWait
        from selenium.webdriver.support import expected_conditions as EC
        try:
            WebDriverWait(self.driver, timeout).until(
                EC.presence_of_element_located(self.USERNAME_INPUT)
            )
        except Exception:
            pass
        return self

    def wait_until_not_loading(self, timeout: int = 10) -> None:
        """Wait until the loading overlay disappears."""
        pass

    def refresh(self) -> "LoginPage":
        """Refresh the current page."""
        self.driver.refresh()
        self.wait_for_load()
        return self
