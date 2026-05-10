"""Screenshot utility for capturing test failures."""
from __future__ import annotations

import os
import time
from pathlib import Path
from typing import TYPE_CHECKING

from core.config_loader import config

if TYPE_CHECKING:
    from selenium.webdriver.remote.webdriver import WebDriver


class ScreenshotUtil:
    """Handles screenshot capture for test reporting."""

    def __init__(self) -> None:
        self.screenshot_dir = config.reports_dir / "screenshots"
        self.screenshot_dir.mkdir(parents=True, exist_ok=True)

    def capture(self, driver: "WebDriver", name: str | None = None) -> str:
        """Capture a screenshot and return the file path."""
        if name is None:
            name = f"screenshot_{int(time.time() * 1000)}"
        filename = f"{name}.png"
        filepath = self.screenshot_dir / filename
        try:
            driver.save_screenshot(str(filepath))
        except Exception:
            pass
        return str(filepath)

    def capture_on_failure(
        self,
        driver: "WebDriver",
        test_name: str,
        module: str = "unknown",
    ) -> str:
        """Capture a screenshot named after the test for failure reporting."""
        safe_name = "".join(c if c.isalnum() or c in ("_", "-") else "_" for c in test_name)
        full_name = f"{module}_{safe_name}"
        return self.capture(driver, full_name)

    def get_screenshot_path(self, name: str) -> str:
        """Return the full path for a screenshot file."""
        return str(self.screenshot_dir / f"{name}.png")

    def cleanup_old_screenshots(self, max_age_hours: int = 24) -> int:
        """Remove screenshots older than max_age_hours. Returns count deleted."""
        deleted = 0
        now = time.time()
        for f in self.screenshot_dir.glob("*.png"):
            if now - f.stat().st_mtime > max_age_hours * 3600:
                try:
                    f.unlink()
                    deleted += 1
                except OSError:
                    pass
        return deleted


screenshot_util = ScreenshotUtil()
