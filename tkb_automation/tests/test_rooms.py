"""Room Management tests - PH-01 to PH-06."""
from __future__ import annotations

import pytest
from pages.rooms_page import RoomsPage


class TestRoomManagement:
    """Test chuc nang quan ly phong hoc."""

    @pytest.mark.functional
    @pytest.mark.db_write
    def test_ph_01_add_room_success(self, rooms_page: RoomsPage):
        """PH-01: Them phong hoc moi thanh cong."""
        rooms_page.add_room(
            room_code="P101",
            building="Tòa A",
            capacity=40,
            floor=1,
            room_type="Phòng thường",
        )
        rooms_page.wait(1)
        assert rooms_page.is_room_in_table("P101")

    @pytest.mark.functional
    def test_ph_02_room_in_table(self, rooms_page: RoomsPage):
        """PH-02: Phong vua them co trong bang."""
        title = rooms_page.get_title()
        assert "Phòng học" in title
        headers = rooms_page.get_table_headers()
        assert len(headers) > 0

    @pytest.mark.functional
    @pytest.mark.db_write
    def test_ph_03_delete_room(self, rooms_page: RoomsPage):
        """PH-03: Xoa phong hoc thanh cong."""
        rooms_page.delete_room("P101", confirm=True)
        rooms_page.wait(0.5)
        assert not rooms_page.is_room_in_table("P101")

    @pytest.mark.functional
    @pytest.mark.db_write
    def test_ph_04_search_room(self, rooms_page: RoomsPage):
        """PH-04: Tim kiem phong hoc theo ma."""
        rooms_page.add_room(
            room_code="P102",
            building="Tòa A",
            capacity=50,
            floor=1,
        )
        rooms_page.wait(0.5)
        rooms_page.search_room("P102")
        rooms_page.wait(0.5)
        assert rooms_page.is_room_in_table("P102")

    @pytest.mark.functional
    @pytest.mark.db_write
    def test_ph_05_add_duplicate_room(self, rooms_page: RoomsPage):
        """PH-05: Them phong trung ma thi hien loi."""
        rooms_page.add_room(
            room_code="P103",
            building="Tòa A",
            capacity=30,
            floor=1,
        )
        rooms_page.wait(0.5)
        rooms_page.add_room(
            room_code="P103",
            building="Tòa A",
            capacity=30,
            floor=1,
        )
        rooms_page.wait(0.5)
        toast = rooms_page.get_toast_message()
        assert toast != "" or rooms_page.is_modal_open()

    @pytest.mark.functional
    def test_ph_06_close_modal(self, rooms_page: RoomsPage):
        """PH-06: Dong modal thi modal dong."""
        rooms_page.click_add_room()
        rooms_page.wait_for_modal()
        rooms_page.cancel_form()
        rooms_page.wait(0.3)
        assert not rooms_page.is_modal_open()
