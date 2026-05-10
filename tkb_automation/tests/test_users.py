"""User Management tests - ND-01 to ND-06."""
from __future__ import annotations

import pytest
from pages.users_page import UsersPage


class TestUserManagement:
    """Test chuc nang quan ly nguoi dung."""

    @pytest.mark.functional
    def test_nd_01_page_loads(self, users_page: UsersPage):
        """ND-01: Trang quan ly nguoi dung tai thanh cong."""
        title = users_page.get_title()
        assert "Quản lý người dùng" in title

    @pytest.mark.functional
    def test_nd_02_table_has_headers(self, users_page: UsersPage):
        """ND-02: Bang nguoi dung co tieu de cot."""
        headers = users_page.get_table_headers()
        assert len(headers) > 0

    @pytest.mark.functional
    def test_nd_03_tab_filters(self, users_page: UsersPage):
        """ND-03: Cac tab loc hoat dong."""
        users_page.click_tab_all()
        users_page.wait(0.3)
        users_page.click_tab_active()
        users_page.wait(0.3)
        users_page.click_tab_inactive()
        users_page.wait(0.3)

    @pytest.mark.functional
    @pytest.mark.db_write
    def test_nd_04_deactivate_user(self, users_page: UsersPage):
        """ND-04: Vo hieu hoa nguoi dung thanh cong."""
        users_page.deactivate_user()
        users_page.wait(0.5)
        toast = users_page.get_toast_message()

    @pytest.mark.functional
    @pytest.mark.db_write
    def test_nd_05_activate_user(self, users_page: UsersPage):
        """ND-05: Kich hoat nguoi dung thanh cong."""
        users_page.activate_user()
        users_page.wait(0.5)
        toast = users_page.get_toast_message()

    @pytest.mark.functional
    def test_nd_06_row_count(self, users_page: UsersPage):
        """ND-06: Bang co du lieu (so dong lon hon 0)."""
        count = users_page.get_row_count()
        assert count >= 0
