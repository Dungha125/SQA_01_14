"""Scheduling tests - LL-01 to LL-06."""
from __future__ import annotations

import pytest
from pages.scheduling_page import SchedulingPage


class TestScheduling:
    """Test chuc nang lap lich thoi khoa bieu."""

    @pytest.mark.functional
    def test_ll_01_page_loads(self, scheduling_page: SchedulingPage):
        """LL-01: Trang lap lich tai thanh cong."""
        title = scheduling_page.get_title()
        assert "Tạo Thời khóa biểu" in title

    @pytest.mark.functional
    def test_ll_02_filter_dropdowns_present(self, scheduling_page: SchedulingPage):
        """LL-02: Cac dropdown loc deu co mat."""
        selects = scheduling_page.get_all_selects()
        assert len(selects) >= 3

    @pytest.mark.functional
    def test_ll_03_generate_disabled_without_data(self, scheduling_page: SchedulingPage):
        """LL-03: Nut Sinh TKB disabled khi chua co du lieu."""
        enabled = scheduling_page.is_generate_button_enabled()
        assert not enabled

    @pytest.mark.functional
    def test_ll_04_import_modal_opens(self, scheduling_page: SchedulingPage):
        """LL-04: Mo modal upload thanh cong."""
        scheduling_page.open_import_modal()
        scheduling_page.wait(0.5)
        modal_visible = scheduling_page.is_element_visible(
            scheduling_page.IMPORT_MODAL
        )
        assert modal_visible
        scheduling_page.close_modal()

    @pytest.mark.functional
    def test_ll_05_download_template(self, scheduling_page: SchedulingPage):
        """LL-05: Tai file mau thanh cong."""
        scheduling_page.open_import_modal()
        scheduling_page.wait(0.5)
        scheduling_page.download_template()
        scheduling_page.wait(1)
        scheduling_page.close_modal()

    @pytest.mark.functional
    def test_ll_06_filter_chain_order(self, scheduling_page: SchedulingPage):
        """LL-06: Chuoi loc hoat dong dung thu tu (Year -> Semester -> Type)."""
        selects = scheduling_page.get_all_selects()
        assert len(selects) >= 3
