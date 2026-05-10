"""Database helper with transaction/rollback support for TKB Automation."""
from __future__ import annotations

from contextlib import contextmanager
from typing import Any, Generator, Optional, Tuple

import pymysql
from pymysql.cursors import DictCursor

from core.config_loader import config


class DBHelper:
    """Direct database access helper with transaction support for data verification and rollback."""

    def __init__(self) -> None:
        self._conn: Optional[pymysql.Connection] = None
        self._cursor: Optional[DictCursor] = None

    def connect(self) -> None:
        if self._conn is None or not self._conn.open:
            self._conn = pymysql.connect(
                host=config.db_host,
                port=config.db_port,
                user=config.db_user,
                password=config.db_password,
                database=config.db_name,
                charset=config.db_charset,
                cursorclass=DictCursor,
                autocommit=False,
            )
            self._cursor = self._conn.cursor()

    def disconnect(self) -> None:
        if self._cursor:
            self._cursor.close()
            self._cursor = None
        if self._conn:
            self._conn.close()
            self._conn = None

    def begin(self) -> None:
        self.connect()
        assert self._conn is not None
        self._conn.begin()

    def commit(self) -> None:
        if self._conn and self._conn.open:
            self._conn.commit()

    def rollback(self) -> None:
        if self._conn and self._conn.open:
            self._conn.rollback()

    @contextmanager
    def transaction(self) -> Generator["DBHelper", None, None]:
        self.begin()
        try:
            yield self
            self.commit()
        except Exception:
            self.rollback()
            raise
        finally:
            self.disconnect()

    def execute(self, query: str, params: Optional[Tuple[Any, ...]] = None) -> list[dict[str, Any]]:
        self.connect()
        assert self._cursor is not None
        self._cursor.execute(query, params)
        return self._cursor.fetchall()

    def execute_write(self, query: str, params: Optional[Tuple[Any, ...]] = None) -> int:
        self.connect()
        assert self._cursor is not None
        self._cursor.execute(query, params)
        self.commit()
        return self._cursor.rowcount

    def get_last_insert_id(self) -> int:
        result = self.execute("SELECT LAST_INSERT_ID() as id")
        return result[0]["id"] if result else 0

    # --- Room helpers ---
    def room_exists(self, room_code: str, building: str) -> bool:
        result = self.execute(
            "SELECT id FROM rooms WHERE room_code = %s AND building = %s",
            (room_code, building),
        )
        return len(result) > 0

    def get_room_by_code(self, room_code: str) -> Optional[dict[str, Any]]:
        result = self.execute("SELECT * FROM rooms WHERE room_code = %s", (room_code,))
        return result[0] if result else None

    def get_room_count(self) -> int:
        result = self.execute("SELECT COUNT(*) as cnt FROM rooms")
        return result[0]["cnt"] if result else 0

    def insert_room(self, room_code: str, building: str, capacity: int,
                   floor: Optional[int] = None, room_type: str = "Phòng thường") -> int:
        return self.execute_write(
            "INSERT INTO rooms (room_code, building, capacity, floor, room_type) VALUES (%s, %s, %s, %s, %s)",
            (room_code, building, capacity, floor, room_type),
        )

    def update_room(self, room_code: str, **kwargs: Any) -> int:
        if not kwargs:
            return 0
        set_clause = ", ".join(f"{k} = %s" for k in kwargs)
        values = list(kwargs.values()) + [room_code]
        return self.execute_write(
            f"UPDATE rooms SET {set_clause} WHERE room_code = %s",
            tuple(values),
        )

    def delete_room(self, room_code: str) -> int:
        return self.execute_write("DELETE FROM rooms WHERE room_code = %s", (room_code,))

    def get_room_capacity(self, room_code: str) -> Optional[int]:
        result = self.execute("SELECT capacity FROM rooms WHERE room_code = %s", (room_code,))
        return result[0]["capacity"] if result else None

    # --- Semester helpers ---
    def semester_exists(self, name: str, year: str) -> bool:
        result = self.execute(
            "SELECT id FROM semesters WHERE name = %s AND academic_year = %s",
            (name, year),
        )
        return len(result) > 0

    def get_semester_count(self, active_only: bool = False) -> int:
        query = "SELECT COUNT(*) as cnt FROM semesters"
        if active_only:
            query += " WHERE is_active = 1"
        result = self.execute(query)
        return result[0]["cnt"] if result else 0

    def get_all_semesters(self) -> list[dict[str, Any]]:
        return self.execute("SELECT * FROM semesters ORDER BY id DESC")

    def insert_semester(self, name: str, year: str, start_date: str,
                        end_date: str, description: str = "",
                        is_active: bool = False) -> int:
        return self.execute_write(
            "INSERT INTO semesters (name, academic_year, start_date, end_date, description, is_active) "
            "VALUES (%s, %s, %s, %s, %s, %s)",
            (name, year, start_date, end_date, description, is_active),
        )

    def delete_semester_by_name(self, name: str, year: str) -> int:
        return self.execute_write(
            "DELETE FROM semesters WHERE name = %s AND academic_year = %s",
            (name, year),
        )

    def activate_semester(self, semester_id: int) -> int:
        self.execute_write("UPDATE semesters SET is_active = 0")
        return self.execute_write(
            "UPDATE semesters SET is_active = 1 WHERE id = %s",
            (semester_id,),
        )

    def deactivate_semester(self, semester_id: int) -> int:
        return self.execute_write(
            "UPDATE semesters SET is_active = 0 WHERE id = %s",
            (semester_id,),
        )

    # --- Subject helpers ---
    def subject_exists(self, subject_code: str) -> bool:
        result = self.execute(
            "SELECT id FROM subjects WHERE subject_code = %s",
            (subject_code,),
        )
        return len(result) > 0

    def get_subject_count(self) -> int:
        result = self.execute("SELECT COUNT(*) as cnt FROM subjects")
        return result[0]["cnt"] if result else 0

    def delete_subject(self, subject_code: str) -> int:
        return self.execute_write(
            "DELETE FROM subjects WHERE subject_code = %s",
            (subject_code,),
        )

    # --- Schedule helpers ---
    def schedule_count(self, semester_id: Optional[int] = None,
                      major_id: Optional[int] = None) -> int:
        conditions = []
        params: list[Any] = []
        if semester_id is not None:
            conditions.append("semester_id = %s")
            params.append(semester_id)
        if major_id is not None:
            conditions.append("major_id = %s")
            params.append(major_id)
        query = "SELECT COUNT(*) as cnt FROM schedules"
        if conditions:
            query += " WHERE " + " AND ".join(conditions)
        result = self.execute(query, tuple(params) if params else None)
        return result[0]["cnt"] if result else 0

    def delete_schedule_by_semester(self, semester_id: int) -> int:
        return self.execute_write(
            "DELETE FROM schedules WHERE semester_id = %s",
            (semester_id,),
        )

    def delete_schedule_by_major(self, semester_id: int, major_id: int) -> int:
        return self.execute_write(
            "DELETE FROM schedules WHERE semester_id = %s AND major_id = %s",
            (semester_id, major_id),
        )

    # --- User helpers ---
    def user_exists(self, username: str) -> bool:
        result = self.execute(
            "SELECT id FROM users WHERE username = %s",
            (username,),
        )
        return len(result) > 0

    def get_user_count(self, active_only: bool = False) -> int:
        query = "SELECT COUNT(*) as cnt FROM users"
        if active_only:
            query += " WHERE is_active = 1 OR enabled = 1"
        result = self.execute(query)
        return result[0]["cnt"] if result else 0

    def get_all_users(self) -> list[dict[str, Any]]:
        return self.execute(
            "SELECT id, username, full_name, email, role, is_active, enabled, created_at FROM users"
        )

    def activate_user(self, username: str) -> int:
        return self.execute_write(
            "UPDATE users SET is_active = 1, enabled = 1 WHERE username = %s",
            (username,),
        )

    def deactivate_user(self, username: str) -> int:
        return self.execute_write(
            "UPDATE users SET is_active = 0, enabled = 0 WHERE username = %s",
            (username,),
        )

    def delete_user(self, username: str) -> int:
        return self.execute_write(
            "DELETE FROM users WHERE username = %s",
            (username,),
        )

    # --- Room occupancy helpers ---
    def update_room_status(self, room_id: int, status: str = "AVAILABLE") -> int:
        return self.execute_write(
            "UPDATE room_occupancy SET status = %s WHERE room_id = %s",
            (status, room_id),
        )

    def get_room_occupancy_count(self, semester_id: int,
                                  status: Optional[str] = None) -> int:
        query = "SELECT COUNT(*) as cnt FROM room_occupancy WHERE semester_id = %s"
        params: list[Any] = [semester_id]
        if status:
            query += " AND status = %s"
            params.append(status)
        result = self.execute(query, tuple(params))
        return result[0]["cnt"] if result else 0
