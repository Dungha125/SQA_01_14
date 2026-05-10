"""Login tests - core authentication functionality."""
from __future__ import annotations

import pytest
from pages.login_page import LoginPage


class TestLogin:
    """Core login tests."""

    def test_login_with_valid_credentials(self, login: LoginPage):
        """DN-01: Login with valid admin credentials succeeds."""
        assert login.is_login_successful(), "Login should redirect away from /login"

    def test_login_button_is_enabled(self, driver):
        """DN-02: Login button is enabled when page loads."""
        page = LoginPage(driver)
        page.open_page()
        assert page.is_login_button_enabled(), "Login button should be enabled"

    def test_login_page_loads(self, driver):
        """DN-03: Login page loads with all required elements."""
        page = LoginPage(driver)
        page.open_page()
        page.wait_for_load()
        assert page.is_element_visible(page.USERNAME_INPUT), "Username input should be visible"
        assert page.is_element_visible(page.PASSWORD_INPUT), "Password input should be visible"
        assert page.is_element_visible(page.LOGIN_BUTTON_ALT), "Login button should be visible"
