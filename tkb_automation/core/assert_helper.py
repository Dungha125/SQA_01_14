"""Custom assertion helpers with automatic screenshot capture on failure."""
from __future__ import annotations

from typing import TYPE_CHECKING, Any, Callable

from selenium.webdriver.remote.webdriver import WebDriver

from core.screenshot_util import screenshot_util

if TYPE_CHECKING:
    from selenium.webdriver.remote.webdriver import WebDriver


class AssertHelper:
    """Custom assertions that automatically capture screenshots on failure."""

    def __init__(self, driver: "WebDriver | None" = None) -> None:
        self._driver = driver

    def set_driver(self, driver: "WebDriver") -> None:
        self._driver = driver

    def _screenshot(self, test_name: str, module: str = "unknown") -> None:
        """Capture screenshot on assertion failure if driver is available."""
        if self._driver:
            screenshot_util.capture_on_failure(self._driver, test_name, module)

    def equal(
        self,
        actual: Any,
        expected: Any,
        message: str = "",
        test_name: str = "assertion",
        module: str = "unknown",
    ) -> None:
        """Assert that two values are equal."""
        if actual != expected:
            self._screenshot(test_name, module)
            raise AssertionError(
                f"{message}\nExpected: {expected!r}\nActual: {actual!r}"
            )

    def not_equal(
        self,
        actual: Any,
        expected: Any,
        message: str = "",
        test_name: str = "assertion",
        module: str = "unknown",
    ) -> None:
        """Assert that two values are not equal."""
        if actual == expected:
            self._screenshot(test_name, module)
            raise AssertionError(
                f"{message}\nExpected NOT: {expected!r}\nBut got: {actual!r}"
            )

    def true(
        self,
        condition: bool,
        message: str = "",
        test_name: str = "assertion",
        module: str = "unknown",
    ) -> None:
        """Assert that a condition is True."""
        if not condition:
            self._screenshot(test_name, module)
            raise AssertionError(message or "Expected True but got False")

    def false(
        self,
        condition: bool,
        message: str = "",
        test_name: str = "assertion",
        module: str = "unknown",
    ) -> None:
        """Assert that a condition is False."""
        if condition:
            self._screenshot(test_name, module)
            raise AssertionError(message or "Expected False but got True")

    def is_none(
        self,
        value: Any,
        message: str = "",
        test_name: str = "assertion",
        module: str = "unknown",
    ) -> None:
        """Assert that a value is None."""
        if value is not None:
            self._screenshot(test_name, module)
            raise AssertionError(message or f"Expected None but got {value!r}")

    def is_not_none(
        self,
        value: Any,
        message: str = "",
        test_name: str = "assertion",
        module: str = "unknown",
    ) -> None:
        """Assert that a value is not None."""
        if value is None:
            self._screenshot(test_name, module)
            raise AssertionError(message or "Expected NOT None but got None")

    def contains(
        self,
        container: Any,
        item: Any,
        message: str = "",
        test_name: str = "assertion",
        module: str = "unknown",
    ) -> None:
        """Assert that a container contains an item."""
        if item not in container:
            self._screenshot(test_name, module)
            raise AssertionError(
                message or f"Container {container!r} does not contain {item!r}"
            )

    def visible(
        self,
        element: Any,
        message: str = "",
        test_name: str = "assertion",
        module: str = "unknown",
    ) -> None:
        """Assert that a WebElement is visible."""
        try:
            if not element.is_displayed():
                self._screenshot(test_name, module)
                raise AssertionError(message or "Element is not visible")
        except Exception as e:
            self._screenshot(test_name, module)
            raise AssertionError(message or f"Element not found: {e}")

    def count_greater(
        self,
        actual: int,
        expected: int,
        message: str = "",
        test_name: str = "assertion",
        module: str = "unknown",
    ) -> None:
        """Assert that actual count is greater than expected."""
        if actual <= expected:
            self._screenshot(test_name, module)
            raise AssertionError(
                message or f"Expected count > {expected}, got {actual}"
            )

    def count_less_or_equal(
        self,
        actual: int,
        expected: int,
        message: str = "",
        test_name: str = "assertion",
        module: str = "unknown",
    ) -> None:
        """Assert that actual count is less than or equal to expected."""
        if actual > expected:
            self._screenshot(test_name, module)
            raise AssertionError(
                message or f"Expected count <= {expected}, got {actual}"
            )

    def in_range(
        self,
        value: float | int,
        min_val: float | int,
        max_val: float | int,
        message: str = "",
        test_name: str = "assertion",
        module: str = "unknown",
    ) -> None:
        """Assert that a value is within a range [min_val, max_val]."""
        if not (min_val <= value <= max_val):
            self._screenshot(test_name, module)
            raise AssertionError(
                message or f"Value {value} is not in range [{min_val}, {max_val}]"
            )

    def with_screenshot(
        self,
        test_name: str,
        module: str = "unknown",
    ) -> "AssertHelper":
        """Return a new AssertHelper that will capture screenshot on any failure."""
        helper = AssertHelper(self._driver)
        return helper
