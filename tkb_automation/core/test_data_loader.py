"""Test data loader - bridges Excel files to pytest test cases."""
from __future__ import annotations

from pathlib import Path
from typing import Any, Generator

from core.config_loader import config
from core.excel_reader import ExcelReader


class TestDataLoader:
    """Loads and provides test data from Excel files for data-driven testing."""

    _instances: dict[str, "TestDataLoader"] = {}

    def __init__(self, module: str) -> None:
        self.module = module
        self._data: dict[str, list[dict[str, Any]]] = {}
        self._load_all()

    def _load_all(self) -> None:
        """Load all data from the module's Excel file."""
        module_files = {
            "rooms": "test_data_rooms.xlsx",
            "semesters": "test_data_semesters.xlsx",
            "scheduling": "test_data_scheduling.xlsx",
            "ctdt": "test_data_ctdt.xlsx",
            "users": "test_data_users.xlsx",
            "tkb": "test_data_tkb.xlsx",
            "hvk": "test_data_hvk.xlsx",
        }
        filename = module_files.get(self.module, f"test_data_{self.module}.xlsx")
        file_path = config.data_dir / filename
        if not file_path.exists():
            return
        try:
            reader = ExcelReader(file_path)
            for sheet_name in reader.get_sheet_names():
                self._data[sheet_name] = reader.read_sheet(sheet_name)
        except Exception:
            pass

    @classmethod
    def get(cls, module: str) -> "TestDataLoader":
        """Get a singleton loader for a module."""
        if module not in cls._instances:
            cls._instances[module] = cls(module)
        return cls._instances[module]

    def get_data(self, sheet: str | None = None) -> list[dict[str, Any]]:
        """Get all test data for the module or from a specific sheet."""
        if sheet:
            return self._data.get(sheet, [])
        for data in self._data.values():
            return data
        return []

    def get_by_id(self, tc_id: str, sheet: str | None = None) -> dict[str, Any] | None:
        """Get a specific test case by its ID."""
        for row in self.get_data(sheet):
            if str(row.get("tc_id", "")) == tc_id:
                return row
        return None

    def filter_by_type(self, test_type: str, sheet: str | None = None) -> list[dict[str, Any]]:
        """Get test cases filtered by test type (UI, Functional, Negative, E2E)."""
        return [
            r for r in self.get_data(sheet)
            if str(r.get("test_type", "")).lower() == test_type.lower()
        ]

    def filter_auto_only(self, sheet: str | None = None) -> list[dict[str, Any]]:
        """Get only test cases marked as auto-executable."""
        return [
            r for r in self.get_data(sheet)
            if "yes" in str(r.get("status", "")).lower()
        ]

    def get_parametrize_data(
        self,
        sheet: str | None = None,
        id_field: str = "tc_id",
        data_field: str = "input_data",
    ) -> list[tuple[str, dict[str, Any]]]:
        """Return data in format suitable for pytest.mark.parametrize."""
        result: list[tuple[str, dict[str, Any]]] = []
        for row in self.get_data(sheet):
            tc_id = str(row.get(id_field, ""))
            if tc_id:
                result.append((tc_id, row))
        return result


def load_module_data(module: str) -> list[dict[str, Any]]:
    """Convenience function to load test data for a module."""
    return TestDataLoader.get(module).get_data()
