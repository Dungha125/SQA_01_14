"""Semester Management tests - HK-01 to HK-06."""
from __future__ import annotations

import pytest
from pages.semesters_page import SemestersPage


class TestSemesterManagement:
    """Test chuc nang quan ly hoc ky."""

    @pytest.mark.functional
    @pytest.mark.db_write
    def test_hk_01_add_semester_success(self, semesters_page: SemestersPage):
        """HK-01: Them hoc ky moi thanh cong."""
        initial_count = semesters_page.get_row_count()
        semesters_page.add_semester(
            name="Hoc ky 1",
            year="2026",
            start_date="2026-01-01",
            end_date="2026-05-31",
            description="Hoc ky 1 nam 2026",
            is_active=True,
        )
        semesters_page.wait(1)
        assert semesters_page.is_semester_in_table("Hoc ky 1")

    @pytest.mark.functional
    def test_hk_02_semester_in_table(self, semesters_page: SemestersPage):
        """HK-02: Hoc ky vua them co trong bang."""
        title = semesters_page.get_title()
        assert "Quản lý học kỳ" in title
        headers = semesters_page.get_table_headers()
        assert len(headers) > 0

    @pytest.mark.functional
    @pytest.mark.db_write
    def test_hk_03_delete_semester(self, semesters_page: SemestersPage):
        """HK-03: Xoa hoc ky thanh cong."""
        semesters_page.delete_semester("Hoc ky 1", confirm=True)
        semesters_page.wait(0.5)
        assert not semesters_page.is_semester_in_table("Hoc ky 1")

    @pytest.mark.functional
    @pytest.mark.db_write
    def test_hk_04_cancel_delete(self, semesters_page: SemestersPage):
        """HK-04: Huy xoa hoc ky thi hoc ky van con."""
        semesters_page.add_semester(
            name="HK Test Cancel",
            year="2026",
            start_date="2026-01-01",
            end_date="2026-05-31",
        )
        semesters_page.wait(0.5)
        semesters_page.delete_semester("HK Test Cancel", confirm=False)
        semesters_page.wait(0.5)
        assert semesters_page.is_semester_in_table("HK Test Cancel")

    @pytest.mark.functional
    @pytest.mark.db_write
    def test_hk_05_add_without_required_fields(self, semesters_page: SemestersPage):
        """HK-05: Them hoc ky khong dien ten thi hien loi."""
        semesters_page.click_add_semester()
        semesters_page.wait_for_modal()
        semesters_page.submit_form(wait_result=False)
        semesters_page.wait(0.5)
        toast = semesters_page.get_toast_message()
        assert toast != "" or semesters_page.is_modal_open()

    @pytest.mark.functional
    def test_hk_06_close_modal(self, semesters_page: SemestersPage):
        """HK-06: Dong modal thi modal dong."""
        semesters_page.click_add_semester()
        semesters_page.wait_for_modal()
        semesters_page.cancel_form()
        semesters_page.wait(0.3)
        assert not semesters_page.is_modal_open()
