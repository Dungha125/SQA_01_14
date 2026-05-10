"""REST API helper for backend verification.

Mirrors the actual frontend API structure from api.ts.
Key differences from api.ts:
  - Login uses /auth/login (no v1 prefix)
  - Token is in response.data.data.token
  - User endpoints use /admin/users
  - Room endpoints use /rooms, /room-occupancies
  - Schedule validation uses /schedule-validation/analyze
"""
from __future__ import annotations

import json
import time
from typing import Any

import requests

from core.config_loader import config


class APIHelper:
    """HTTP client for direct API calls to the backend, used for data verification."""

    def __init__(self, base_url: str | None = None) -> None:
        # NOTE: frontend api.ts uses http://localhost:8081/api (NOT /api/v1)
        self.base_url = base_url or config.api_url
        self.session = requests.Session()
        self.session.headers.update({
            "Content-Type": "application/json",
            "Accept": "application/json",
        })
        self._token: str | None = None

    def login(self, username: str | None = None, password: str | None = None) -> dict[str, Any]:
        """Authenticate and store the JWT token.

        Backend response: {success, message, data: {id, username, email, fullName, role, token}}
        """
        user = username or config.auth_username
        pwd = password or config.auth_password
        try:
            resp = self.session.post(
                f"{self.base_url}/auth/login",
                json={"username": user, "password": pwd},
                timeout=config.timeout,
            )
            resp.raise_for_status()
            raw = resp.json()
            # Frontend expects response.data.data.token
            data = raw.get("data", {})
            token = data.get("token")
            if token:
                self._token = token
                self.session.headers.update({"Authorization": f"Bearer {token}"})
            return raw
        except requests.RequestException as e:
            return {"error": str(e), "status_code": getattr(e.response, "status_code", None)}

    def get(self, endpoint: str, params: dict[str, Any] | None = None) -> dict[str, Any]:
        try:
            resp = self.session.get(
                f"{self.base_url}{endpoint}",
                params=params,
                timeout=config.timeout,
            )
            resp.raise_for_status()
            return resp.json()
        except requests.RequestException as e:
            return {"error": str(e), "status_code": getattr(e.response, "status_code", None)}

    def post(self, endpoint: str, data: dict[str, Any] | None = None) -> dict[str, Any]:
        try:
            resp = self.session.post(
                f"{self.base_url}{endpoint}",
                json=data,
                timeout=config.timeout,
            )
            resp.raise_for_status()
            return resp.json()
        except requests.RequestException as e:
            return {"error": str(e), "status_code": getattr(e.response, "status_code", None)}

    def put(self, endpoint: str, data: dict[str, Any] | None = None) -> dict[str, Any]:
        try:
            resp = self.session.put(
                f"{self.base_url}{endpoint}",
                json=data,
                timeout=config.timeout,
            )
            resp.raise_for_status()
            return resp.json()
        except requests.RequestException as e:
            return {"error": str(e), "status_code": getattr(e.response, "status_code", None)}

    def patch(self, endpoint: str, data: dict[str, Any] | None = None) -> dict[str, Any]:
        try:
            resp = self.session.patch(
                f"{self.base_url}{endpoint}",
                json=data,
                timeout=config.timeout,
            )
            resp.raise_for_status()
            return resp.json()
        except requests.RequestException as e:
            return {"error": str(e), "status_code": getattr(e.response, "status_code", None)}

    def delete(self, endpoint: str) -> dict[str, Any]:
        try:
            resp = self.session.delete(
                f"{self.base_url}{endpoint}",
                timeout=config.timeout,
            )
            resp.raise_for_status()
            return resp.json()
        except requests.RequestException as e:
            return {"error": str(e), "status_code": getattr(e.response, "status_code", None)}

    def upload_file(self, endpoint: str, file_path: str,
                    file_field: str = "file",
                    extra_data: dict[str, Any] | None = None) -> dict[str, Any]:
        try:
            files: dict[str, Any] = {}
            files[file_field] = open(file_path, "rb")
            data: dict[str, Any] = extra_data or {}
            resp = self.session.post(
                f"{self.base_url}{endpoint}",
                files=files,
                data=data,
                timeout=60,
            )
            return resp.json()
        except requests.RequestException as e:
            return {"error": str(e), "status_code": getattr(e.response, "status_code", None)}
        finally:
            try:
                if "files" in dir() and file_field in files:
                    files[file_field].close()
            except Exception:
                pass

    @property
    def is_authenticated(self) -> bool:
        return self._token is not None

    def close(self) -> None:
        self.session.close()

    # ===== Rooms =====

    def get_rooms(self, page: int = 0, size: int = 100,
                  search: str = "", building: str = "") -> dict[str, Any]:
        params: dict[str, Any] = {"page": page, "size": size}
        if search:
            params["search"] = search
        if building:
            params["building"] = building
        return self.get("/rooms", params)

    def create_room(self, room_code: str, building: str,
                    capacity: int, room_type: str = "GENERAL",
                    floor: int | None = None,
                    status: str = "AVAILABLE") -> dict[str, Any]:
        data: dict[str, Any] = {
            "name": room_code,
            "building": building,
            "capacity": capacity,
            "type": room_type,
            "status": status,
        }
        if floor is not None:
            data["floor"] = floor
        return self.post("/rooms", data)

    def update_room(self, room_id: int, **kwargs: Any) -> dict[str, Any]:
        return self.put(f"/rooms/{room_id}", kwargs)

    def delete_room(self, room_id: int) -> dict[str, Any]:
        return self.delete(f"/rooms/{room_id}")

    def get_room_count(self) -> int:
        result = self.get_rooms(page=0, size=1)
        try:
            return result.get("data", {}).get("totalElements", 0)
        except Exception:
            return 0

    # ===== Semesters =====

    def get_semesters(self) -> dict[str, Any]:
        return self.get("/semesters")

    def create_semester(self, semester_name: str, academic_year: str,
                        start_date: str = "", end_date: str = "",
                        description: str = "",
                        is_active: bool = False) -> dict[str, Any]:
        return self.post("/semesters", {
            "semesterName": semester_name,
            "academicYear": academic_year,
            "startDate": start_date,
            "endDate": end_date,
            "description": description,
            "isActive": is_active,
        })

    def update_semester(self, semester_id: int, **kwargs: Any) -> dict[str, Any]:
        return self.put(f"/semesters/{semester_id}", kwargs)

    def delete_semester(self, semester_id: int) -> dict[str, Any]:
        return self.delete(f"/semesters/{semester_id}")

    def set_semester_active(self, semester_id: int) -> dict[str, Any]:
        return self.patch(f"/semesters/{semester_id}/activate", {})

    def get_semester_count(self) -> int:
        result = self.get_semesters()
        try:
            return len(result.get("data", []))
        except Exception:
            return 0

    # ===== Subjects (CTDT) =====

    def get_subjects(self, page: int = 0, size: int = 15,
                     search: str = "", semester: str = "",
                     class_year: str = "", major_code: str = "",
                     program_type: str = "",
                     academic_year: str = "") -> dict[str, Any]:
        params: dict[str, Any] = {
            "page": page, "size": size,
            "sortBy": "id", "sortDir": "asc"
        }
        if search:
            params["search"] = search
        if semester:
            params["semester"] = semester
        if class_year:
            params["classYear"] = class_year
        if major_code:
            params["majorCode"] = major_code
        if program_type:
            params["programType"] = program_type
        if academic_year:
            params["academicYear"] = academic_year
        return self.get("/subjects", params)

    def create_subject(self, data: dict[str, Any]) -> dict[str, Any]:
        return self.post("/subjects", data)

    def delete_subject(self, subject_id: int) -> dict[str, Any]:
        return self.delete(f"/subjects/{subject_id}")

    def upload_subjects_excel(self, file_path: str, semester: str) -> dict[str, Any]:
        return self.upload_file("/subjects/upload-excel", file_path, "file",
                               {"semester": semester})

    # ===== Schedule Validation =====

    def analyze_schedule(self, file_path: str) -> dict[str, Any]:
        return self.upload_file("/schedule-validation/analyze", file_path, "file")

    def validate_schedule_format(self, file_path: str) -> dict[str, Any]:
        return self.upload_file("/schedule-validation/validate-format", file_path, "file")

    # ===== Users =====

    def get_users(self) -> dict[str, Any]:
        return self.get("/admin/users")

    def toggle_user_status(self, user_id: int, enabled: bool) -> dict[str, Any]:
        return self.patch(f"/admin/users/{user_id}/toggle-status", {"enabled": enabled})

    def delete_user(self, user_id: int) -> dict[str, Any]:
        return self.delete(f"/admin/users/{user_id}")

    # ===== Schedule =====

    def get_saved_schedules(self, academic_year: str = "",
                            semester: str = "") -> dict[str, Any]:
        params: dict[str, Any] = {}
        if academic_year:
            params["academicYear"] = academic_year
        if semester:
            params["semester"] = semester
        return self.get("/saved-schedules", params)
