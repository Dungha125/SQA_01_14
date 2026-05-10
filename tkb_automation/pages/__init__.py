"""Pages module initialization."""
from pages.base_page import BasePage
from pages.login_page import LoginPage
from pages.rooms_page import RoomsPage
from pages.semesters_page import SemestersPage
from pages.scheduling_page import SchedulingPage
from pages.ctdt_page import CTDTPage
from pages.users_page import UsersPage
from pages.tkb_page import TKBPage
from pages.hvk_page import HVKPage

__all__ = [
    "BasePage",
    "LoginPage",
    "RoomsPage",
    "SemestersPage",
    "SchedulingPage",
    "CTDTPage",
    "UsersPage",
    "TKBPage",
    "HVKPage",
]
