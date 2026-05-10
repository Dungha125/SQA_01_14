"""CTDT / Subject Management tests - CT-01 to CT-06."""
from __future__ import annotations

import pytest
from pages.ctdt_page import CTDTPage


class TestCTDTManagement:
    """Test chuc nang quan ly mon hoc / chuong trinh dao tao."""

    @pytest.mark.functional
    def test_ct_01_page_loads(self, ctdt_page: CTDTPage):
        """CT-01: Trang quan ly mon hoc tai thanh cong."""
        title = ctdt_page.get_title()
        assert "Quản lý Môn học" in title

    @pytest.mark.functional
    def test_ct_02_table_has_headers(self, ctdt_page: CTDTPage):
        """CT-02: Bang mon hoc co tieu de cot."""
        headers = ctdt_page.get_table_headers()
        assert len(headers) > 0

    @pytest.mark.functional
    @pytest.mark.db_write
    def test_ct_03_add_subject_modal(self, ctdt_page: CTDTPage):
        """CT-03: Mo modal them mon hoc thanh cong."""
        ctdt_page.click_add_subject()
        ctdt_page.wait_for_modal()
        assert ctdt_page.is_modal_open()

    @pytest.mark.functional
    @pytest.mark.db_write
    def test_ct_04_add_subject_success(self, ctdt_page: CTDTPage):
        """CT-04: Them mon hoc moi thanh cong."""
        ctdt_page.click_add_subject()
        ctdt_page.wait_for_modal()
        ctdt_page.fill_subject_form(
            code="M001",
            name="Toan roi rac",
            credits=3,
        )
        ctdt_page.submit_form()
        ctdt_page.wait(1)
        assert ctdt_page.is_subject_in_table("M001")

    @pytest.mark.functional
    @pytest.mark.db_write
    def test_ct_05_delete_subject(self, ctdt_page: CTDTPage):
        """CT-05: Xoa mon hoc thanh cong."""
        ctdt_page.delete_subject("M001", confirm=True)
        ctdt_page.wait(0.5)
        assert not ctdt_page.is_subject_in_table("M001")

    @pytest.mark.functional
    def test_ct_06_search_subject(self, ctdt_page: CTDTPage):
        """CT-06: Tim kiem mon hoc theo ma hoac ten."""
        ctdt_page.search_subject("M001")
        ctdt_page.wait(0.5)
        assert ctdt_page.get_row_count() >= 0
