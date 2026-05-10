"""Configuration loader for the TKB Automation Framework."""
from __future__ import annotations

import os
from pathlib import Path
from typing import Any

import yaml


class Config:
    """Central configuration manager that loads settings from config.yaml and environment variables."""

    _instance: Config | None = None
    _config: dict[str, Any] = {}

    def __new__(cls) -> Config:
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance._load()
        return cls._instance

    def _load(self) -> None:
        """Load configuration from YAML and environment variables."""
        config_path = Path(__file__).parent.parent / "config" / "config.yaml"
        if config_path.exists():
            with open(config_path, "r", encoding="utf-8") as f:
                self._config = yaml.safe_load(f) or {}
        self._apply_env_overrides()

    def _apply_env_overrides(self) -> None:
        """Override config values with environment variables."""
        env_mappings = {
            "APP_BASE_URL": ("app", "base_url"),
            "API_BASE_URL": ("app", "api_base_url"),
            "HEADLESS": ("app", "headless"),
            "DB_HOST": ("database", "host"),
            "DB_PORT": ("database", "port"),
            "DB_USER": ("database", "user"),
            "DB_PASSWORD": ("database", "password"),
            "DB_NAME": ("database", "database"),
            "AUTH_USERNAME": ("auth", "username"),
            "AUTH_PASSWORD": ("auth", "password"),
            "BROWSER": ("browser", "default"),
            "MAX_WORKERS": ("parallel", "max_workers"),
        }
        for env_key, (section, key) in env_mappings.items():
            value = os.environ.get(env_key)
            if value is not None:
                if section not in self._config:
                    self._config[section] = {}
                if env_key == "HEADLESS":
                    self._config[section][key] = value.lower() in ("true", "1", "yes")
                elif env_key in ("DB_PORT", "MAX_WORKERS"):
                    self._config[section][key] = int(value)
                else:
                    self._config[section][key] = value

    def get(self, *keys: str, default: Any = None) -> Any:
        """Get a nested config value using dot-notation or multiple keys."""
        val = self._config
        for key in keys:
            if isinstance(val, dict):
                val = val.get(key)
            else:
                return default
            if val is None:
                return default
        return val

    @property
    def app_url(self) -> str:
        return self.get("app", "base_url", default="http://localhost:3000")

    @property
    def api_url(self) -> str:
        return self.get("app", "api_base_url", default="http://localhost:8081/api/v1")

    @property
    def timeout(self) -> int:
        return self.get("app", "timeout", default=15)

    @property
    def implicit_wait(self) -> int:
        return self.get("app", "implicit_wait", default=5)

    @property
    def headless(self) -> bool:
        return self.get("app", "headless", default=False)

    @property
    def window_width(self) -> int:
        return self.get("app", "window_width", default=1920)

    @property
    def window_height(self) -> int:
        return self.get("app", "window_height", default=1080)

    @property
    def db_host(self) -> str:
        return self.get("database", "host", default="localhost")

    @property
    def db_port(self) -> int:
        return self.get("database", "port", default=3306)

    @property
    def db_user(self) -> str:
        return self.get("database", "user", default="root")

    @property
    def db_password(self) -> str:
        return self.get("database", "password", default="")

    @property
    def db_name(self) -> str:
        return self.get("database", "database", default="schedule")

    @property
    def db_charset(self) -> str:
        return self.get("database", "charset", default="utf8mb4")

    @property
    def auth_username(self) -> str:
        return self.get("auth", "username", default="admin")

    @property
    def auth_password(self) -> str:
        return self.get("auth", "password", default="admin123")

    @property
    def login_endpoint(self) -> str:
        return self.get("auth", "login_endpoint", default="/api/auth/login")

    @property
    def browser(self) -> str:
        return self.get("browser", "default", default="chrome")

    @property
    def download_dir(self) -> str:
        return self.get("browser", "download_dir", default="downloads")

    @property
    def max_workers(self) -> int:
        return self.get("parallel", "max_workers", default=4)

    @property
    def screenshot_on_failure(self) -> bool:
        return self.get("app", "screenshot_on_failure", default=True)

    @property
    def reports_dir(self) -> Path:
        return Path(__file__).parent.parent / "reports"

    @property
    def data_dir(self) -> Path:
        return Path(__file__).parent.parent.parent / "data"

    @property
    def logs_dir(self) -> Path:
        return Path(__file__).parent.parent / "logs"


config = Config()
