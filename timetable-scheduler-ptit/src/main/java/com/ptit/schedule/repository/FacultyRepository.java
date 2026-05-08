package com.ptit.schedule.repository;

import com.ptit.schedule.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, String> {
    
    // Tìm faculty theo tên (tìm kiếm gần đúng)
    List<Faculty> findByFacultyNameContainingIgnoreCase(String facultyName);
}
