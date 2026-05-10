"""Post-Validation (Hau kiem) Page Object Model."""
from __future__ import annotations

from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webdriver import WebDriver

from pages.base_page import BasePage


class HVKPage(BasePage):
    """Page Object for Post-Validation (Hau kiem) page.

    Frontend: ScheduleValidationPage.tsx.
    URL: /schedule-validation
    Has file upload, drag-drop zone, analyze button, conflict result cards.
    """

    URL = "/schedule-validation"

    # ===== Locators =====

    PAGE_TITLE = (By.XPATH, "//h1[contains(text(),'Hậu kiểm')]")

    # Upload section
    FILE_INPUT = (By.CSS_SELECTOR, "input[type='file']")
    SELECT_FILE_BUTTON = (By.XPATH, "//button[contains(text(),'Chọn file')]")
    DOWNLOAD_TEMPLATE_BUTTON = (By.XPATH, "//button[contains(text(),'Tải file mẫu')]")
    ANALYZE_BUTTON = (By.XPATH, "//button[contains(text(),'Kiểm tra xung đột')]")
    DROPZONE = (By.CSS_SELECTOR, "div.border-2.border-dashed")
    INFO_CARD = (By.XPATH, "//div[contains(@class,'bg-blue') or contains(@class,'bg-indigo') or contains(@class,'bg-cyan')]//p[contains(text(),'Hậu kiểm')]")

    # Results
    RESULTS_HEADER = (By.XPATH, "//h3[contains(text(),'Kết quả')]")
    RESET_BUTTON = (By.XPATH, "//button[contains(text(),'Kiểm tra file khác')]")

    # Conflict summary cards
    TOTAL_CONFLICT_COUNT = (By.CSS_SELECTOR, "div.text-xl.font-bold.text-red-600")
    ROOM_CONFLICT_COUNT = (By.CSS_SELECTOR, "div.text-xl.font-bold.text-orange-600")
    TEACHER_CONFLICT_COUNT = (By.CSS_SELECTOR, "div.text-xl.font-bold.text-purple-600")

    # Conflict panels
    ROOM_CONFLICT_PANEL = (By.CSS_SELECTOR, "div.border.border-red-200, div.border.border-orange-200")
    TEACHER_CONFLICT_PANEL = (By.CSS_SELECTOR, "div.border.border-purple-200")
    NO_CONFLICT_PANEL = (By.CSS_SELECTOR, "div.border-l-4.border-l-green-500")
    CONFLICT_CARDS = (By.CSS_SELECTOR, "div.bg-gray-50.rounded-md")

    # Toast
    TOAST = (By.CSS_SELECTOR, "[data-hot-toast]")

    def __init__(self, driver: WebDriver) -> None:
        super().__init__(driver)
        self.url = self.URL

    def open_page(self) -> "HVKPage":
        from core.config_loader import config
        self.driver.get(f"{config.app_url}{self.URL}")
        self.wait_for_load()
        return self

    def wait_for_load(self, timeout: int = 10) -> None:
        try:
            self.wait_until_not_loading(timeout)
            self.wait(0.5)
        except Exception:
            pass

    def get_title(self) -> str:
        try:
            return self.get_text(self.PAGE_TITLE)
        except Exception:
            return ""

    def get_dropzone_text(self) -> str:
        try:
            return self.get_text(self.DROPZONE)
        except Exception:
            return ""

    # ===== Upload =====

    def upload_file(self, file_path: str) -> "HVKPage":
        try:
            file_input = self.find_element_visible(self.FILE_INPUT)
            file_input.send_keys(file_path)
            self.wait(1)
        except Exception:
            pass
        return self

    def click_select_file(self) -> "HVKPage":
        try:
            self.click(self.SELECT_FILE_BUTTON)
            self.wait(0.5)
        except Exception:
            pass
        return self

    def download_template(self) -> "HVKPage":
        try:
            self.click(self.DOWNLOAD_TEMPLATE_BUTTON)
            self.wait(1)
        except Exception:
            pass
        return self

    # ===== Analyze =====

    def is_analyze_disabled(self) -> bool:
        try:
            btn = self.find_element_visible(self.ANALYZE_BUTTON)
            return not btn.is_enabled()
        except Exception:
            return True

    def click_analyze(self) -> "HVKPage":
        try:
            self.click(self.ANALYZE_BUTTON)
            self.wait(2)
        except Exception:
            pass
        return self

    def click_check_another(self) -> "HVKPage":
        try:
            self.click(self.RESET_BUTTON)
            self.wait(0.5)
        except Exception:
            pass
        return self

    # ===== Results =====

    def is_upload_area_visible(self) -> bool:
        return self.is_element_visible(self.FILE_INPUT)

    def get_total_conflict_count(self) -> int:
        try:
            text = self.get_text(self.TOTAL_CONFLICT_COUNT)
            import re
            m = re.search(r"(\d+)", text)
            return int(m.group(1)) if m else 0
        except Exception:
            return 0

    def get_room_conflict_count(self) -> int:
        try:
            text = self.get_text(self.ROOM_CONFLICT_COUNT)
            import re
            m = re.search(r"(\d+)", text)
            return int(m.group(1)) if m else 0
        except Exception:
            return 0

    def get_teacher_conflict_count(self) -> int:
        try:
            text = self.get_text(self.TEACHER_CONFLICT_COUNT)
            import re
            m = re.search(r"(\d+)", text)
            return int(m.group(1)) if m else 0
        except Exception:
            return 0

    def has_conflicts(self) -> bool:
        return self.get_total_conflict_count() > 0

    def no_conflicts(self) -> bool:
        return self.get_total_conflict_count() == 0

    def get_conflict_panels(self) -> int:
        try:
            panels = self.find_elements(self.CONFLICT_CARDS)
            return len([p for p in panels if p.is_displayed()])
        except Exception:
            return 0

    # ===== Verifications =====

    def get_toast_message(self, timeout: int = 5) -> str:
        try:
            toast = self.find_element_visible(self.TOAST, timeout=timeout)
            return toast.text
        except Exception:
            return ""
