"""WebDriver factory for TKB Automation Framework."""
from __future__ import annotations

import os
import time
from pathlib import Path
from typing import TYPE_CHECKING

from selenium import webdriver
from selenium.webdriver.chrome.options import Options as ChromeOptions
from selenium.webdriver.chrome.service import Service as ChromeService
from selenium.webdriver.edge.options import Options as EdgeOptions
from selenium.webdriver.edge.service import Service as EdgeService
from selenium.webdriver.firefox.options import Options as FirefoxOptions
from selenium.webdriver.firefox.service import Service as FirefoxService
from selenium.webdriver.support.ui import WebDriverWait

if TYPE_CHECKING:
    from selenium.webdriver.remote.webdriver import WebDriver

from core.config_loader import config


class BrowserFactory:
    """Factory class for creating and configuring WebDriver instances."""

    _drivers: dict[str, "WebDriver"] = {}

    @classmethod
    def create_driver(cls, browser_name: str | None = None) -> "WebDriver":
        """Create and configure a WebDriver instance.

        Args:
            browser_name: One of 'chrome', 'firefox', 'edge'. Defaults to config value.

        Returns:
            Configured WebDriver instance.
        """
        browser = (browser_name or config.browser).lower()

        if browser == "chrome":
            return cls._create_chrome()
        elif browser == "firefox":
            return cls._create_firefox()
        elif browser == "edge":
            return cls._create_edge()
        else:
            return cls._create_chrome()

    @classmethod
    def _create_chrome(cls) -> "WebDriver":
        options = ChromeOptions()
        if config.headless:
            options.add_argument("--headless=new")
        options.add_argument(f"--window-size={config.window_width},{config.window_height}")
        options.add_argument("--no-sandbox")
        options.add_argument("--disable-dev-shm-usage")
        options.add_argument("--disable-gpu")
        options.add_argument("--disable-extensions")
        options.add_argument("--disable-logging")
        options.add_argument("--log-level=3")
        options.add_argument("--start-maximized")
        options.add_argument("--ignore-certificate-errors")
        prefs = {
            "profile.default_content_settings.popups": 0,
            "download.prompt_for_download": False,
            "download.default_directory": str(Path(__file__).parent.parent / config.download_dir),
            "directory_upgrade": True,
        }
        options.add_experimental_option("prefs", prefs)
        options.add_experimental_option("excludeSwitches", ["enable-logging"])
        options.page_load_strategy = "normal"

        driver_path = config.get("browser", "drivers", "chrome")
        if driver_path and os.path.exists(driver_path):
            service = ChromeService(executable_path=driver_path)
            driver = webdriver.Chrome(service=service, options=options)
        else:
            try:
                from webdriver_manager.chrome import ChromeDriverManager
                service = ChromeService(ChromeDriverManager().install())
                driver = webdriver.Chrome(service=service, options=options)
            except Exception:
                driver = webdriver.Chrome(options=options)

        cls._configure_driver(driver)
        return driver

    @classmethod
    def _create_firefox(cls) -> "WebDriver":
        options = FirefoxOptions()
        if config.headless:
            options.add_argument("--headless")
        options.add_argument(f"--width={config.window_width}")
        options.add_argument(f"--height={config.window_height}")
        options.set_preference("browser.download.folderList", 2)
        options.set_preference("browser.download.dir", str(Path(__file__).parent.parent / config.download_dir))
        options.set_preference("browser.helperApps.neverAsk.saveToDisk", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        options.set_preference("pdfjs.disabled", True)

        driver_path = config.get("browser", "drivers", "firefox")
        if driver_path and os.path.exists(driver_path):
            service = FirefoxService(executable_path=driver_path)
            driver = webdriver.Firefox(service=service, options=options)
        else:
            try:
                from webdriver_manager.firefox import GeckoDriverManager
                service = FirefoxService(GeckoDriverManager().install())
                driver = webdriver.Firefox(service=service, options=options)
            except Exception:
                driver = webdriver.Firefox(options=options)

        cls._configure_driver(driver)
        return driver

    @classmethod
    def _create_edge(cls) -> "WebDriver":
        options = EdgeOptions()
        if config.headless:
            options.add_argument("--headless=new")
        options.add_argument(f"--window-size={config.window_width},{config.window_height}")
        options.add_argument("--no-sandbox")
        options.add_argument("--disable-dev-shm-usage")
        options.add_argument("--disable-gpu")
        prefs = {
            "profile.default_content_settings.popups": 0,
            "download.prompt_for_download": False,
            "download.default_directory": str(Path(__file__).parent.parent / config.download_dir),
        }
        options.add_experimental_option("prefs", prefs)

        driver_path = config.get("browser", "drivers", "edge")
        if driver_path and os.path.exists(driver_path):
            service = EdgeService(executable_path=driver_path)
            driver = webdriver.Edge(service=service, options=options)
        else:
            try:
                from webdriver_manager.microsoft import EdgeChromiumDriverManager
                service = EdgeService(EdgeChromiumDriverManager().install())
                driver = webdriver.Edge(service=service, options=options)
            except Exception:
                driver = webdriver.Edge(options=options)

        cls._configure_driver(driver)
        return driver

    @classmethod
    def _configure_driver(cls, driver: "WebDriver") -> None:
        """Apply common configuration to a WebDriver instance."""
        driver.implicitly_wait(config.implicit_wait)
        driver.set_page_load_timeout(config.timeout)
        driver.maximize_window()

    @classmethod
    def wait_for(cls, driver: "WebDriver", timeout: int | None = None) -> WebDriverWait:
        """Create a WebDriverWait with the configured timeout."""
        return WebDriverWait(driver, timeout or config.timeout)

    @classmethod
    def quit_driver(cls, driver: "WebDriver") -> None:
        """Safely quit a WebDriver instance."""
        try:
            driver.quit()
        except Exception:
            pass

    @classmethod
    def close_all(cls) -> None:
        """Close all managed driver instances."""
        for driver in cls._drivers.values():
            try:
                driver.quit()
            except Exception:
                pass
        cls._drivers.clear()
