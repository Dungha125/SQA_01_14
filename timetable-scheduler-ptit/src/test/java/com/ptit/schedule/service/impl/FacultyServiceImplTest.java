package com.ptit.schedule.service.impl;

import com.ptit.schedule.dto.FacultyRequest;
import com.ptit.schedule.dto.FacultyResponse;
import com.ptit.schedule.entity.Faculty;
import com.ptit.schedule.exception.ResourceNotFoundException;
import com.ptit.schedule.repository.FacultyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test Suite FacultyServiceImpl - Kiểm thử Service Khoa")
class FacultyServiceImplTest {

    private static final Logger logger = LoggerFactory.getLogger(FacultyServiceImplTest.class);

    @Mock
    private FacultyRepository facultyRepository;

    @InjectMocks
    private FacultyServiceImpl facultyService;

    private Faculty testFaculty1;
    private Faculty testFaculty2;
    private String facultyId1;
    private String facultyId2;

    @BeforeEach
    void setUp() {
        facultyId1 = UUID.randomUUID().toString();
        facultyId2 = UUID.randomUUID().toString();

        testFaculty1 = new Faculty();
        testFaculty1.setId(facultyId1);
        testFaculty1.setFacultyName("Công nghệ thông tin");

        testFaculty2 = new Faculty();
        testFaculty2.setId(facultyId2);
        testFaculty2.setFacultyName("Điện tử - Viễn thông");
    }

    @Test
    @DisplayName("DT001 - Get all faculties successfully")
    void testGetAllFacultiesSuccess() {
        // Arrange
        List<Faculty> faculties = Arrays.asList(testFaculty1, testFaculty2);
        when(facultyRepository.findAll()).thenReturn(faculties);
        logger.info("TC001 - Input: getAllFaculties()");

        // Act
        List<FacultyResponse> responses = facultyService.getAllFaculties();

        // Assert
        logger.info("TC001 - Output: responseCount={}, firstFacultyName={}", 
            responses.size(), responses.isEmpty() ? null : responses.get(0).getFacultyName());
        
        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getFacultyName()).isEqualTo("Công nghệ thông tin");
        verify(facultyRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("DT002 - Get all faculties when empty")
    void testGetAllFacultiesEmpty() {
        // Arrange
        when(facultyRepository.findAll()).thenReturn(Arrays.asList());
        logger.info("TC002 - Input: getAllFaculties() with empty result");

        // Act
        List<FacultyResponse> responses = facultyService.getAllFaculties();

        // Assert
        logger.info("TC002 - Output: responseCount={}", responses.size());
        
        assertThat(responses).isEmpty();
        verify(facultyRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("DT003 - Get faculty by ID successfully")
    void testGetFacultyByIdSuccess() {
        // Arrange
        when(facultyRepository.findById(facultyId1)).thenReturn(Optional.of(testFaculty1));
        logger.info("TC003 - Input: getFacultyById={}", facultyId1);

        // Act
        FacultyResponse response = facultyService.getFacultyById(facultyId1);

        // Assert
        logger.info("TC003 - Output: facultyId={}, facultyName={}", 
            response.getId(), response.getFacultyName());
        
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(facultyId1);
        assertThat(response.getFacultyName()).isEqualTo("Công nghệ thông tin");
        verify(facultyRepository, times(1)).findById(facultyId1);
    }

    @Test
    @DisplayName("DT004 - Get faculty by ID not found throws exception")
    void testGetFacultyByIdNotFound() {
        // Arrange
        when(facultyRepository.findById(anyString())).thenReturn(Optional.empty());
        logger.info("TC004 - Input: getFacultyById={}", "nonexistent");

        // Act & Assert
        logger.info("TC004 - Output: exceptionThrown={}", true);
        
        assertThatThrownBy(() -> facultyService.getFacultyById("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("DT005 - Create faculty successfully")
    void testCreateFacultySuccess() {
        // Arrange
        FacultyRequest request = new FacultyRequest();
        request.setFacultyName("Cơ khí");

        Faculty newFaculty = new Faculty();
        newFaculty.setId(facultyId1);
        newFaculty.setFacultyName("Cơ khí");

        when(facultyRepository.save(any(Faculty.class))).thenReturn(newFaculty);
        logger.info("TC005 - Input: facultyName={}", request.getFacultyName());

        // Act
        FacultyResponse response = facultyService.createFaculty(request);

        // Assert
        logger.info("TC005 - Output: savedId={}, savedName={}", 
            response.getId(), response.getFacultyName());
        
        assertThat(response).isNotNull();
        assertThat(response.getFacultyName()).isEqualTo("Cơ khí");
        assertThat(response.getId()).isNotNull();
        verify(facultyRepository, times(1)).save(any(Faculty.class));
    }

    @Test
    @DisplayName("DT006 - Update faculty successfully")
    void testUpdateFacultySuccess() {
        // Arrange
        FacultyRequest request = new FacultyRequest();
        request.setFacultyName("Công nghệ thông tin - Updated");

        when(facultyRepository.findById(facultyId1)).thenReturn(Optional.of(testFaculty1));
        when(facultyRepository.save(any(Faculty.class))).thenReturn(testFaculty1);
        logger.info("TC006 - Input: facultyId={}, newFacultyName={}", 
            facultyId1, request.getFacultyName());

        // Act
        FacultyResponse response = facultyService.updateFaculty(facultyId1, request);

        // Assert
        logger.info("TC006 - Output: updateSuccess={}, facultyId={}", true, response.getId());
        
        assertThat(response).isNotNull();
        verify(facultyRepository, times(1)).findById(facultyId1);
        verify(facultyRepository, times(1)).save(any(Faculty.class));
    }

    @Test
    @DisplayName("DT007 - Update non-existent faculty throws exception")
    void testUpdateFacultyNotFound() {
        // Arrange
        FacultyRequest request = new FacultyRequest();
        request.setFacultyName("Updated Name");
        when(facultyRepository.findById(anyString())).thenReturn(Optional.empty());
        logger.info("TC007 - Input: facultyId={}, newFacultyName={}", "nonexistent", "Updated Name");

        // Act & Assert
        logger.info("TC007 - Output: exceptionThrown={}", true);
        
        assertThatThrownBy(() -> facultyService.updateFaculty("nonexistent", request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(facultyRepository, never()).save(any());
    }

    @Test
    @DisplayName("DT008 - Delete faculty successfully")
    void testDeleteFacultySuccess() {
        // Arrange
        when(facultyRepository.existsById(facultyId1)).thenReturn(true);
        doNothing().when(facultyRepository).deleteById(facultyId1);
        logger.info("TC008 - Input: facultyId={}", facultyId1);

        // Act
        facultyService.deleteFaculty(facultyId1);

        // Assert
        logger.info("TC008 - Output: deleteSuccess={}", true);
        
        verify(facultyRepository, times(1)).existsById(facultyId1);
        verify(facultyRepository, times(1)).deleteById(facultyId1);
    }

    @Test
    @DisplayName("DT009 - Delete non-existent faculty throws exception")
    void testDeleteFacultyNotFound() {
        // Arrange
        when(facultyRepository.existsById(anyString())).thenReturn(false);
        logger.info("TC009 - Input: facultyId={}", "nonexistent");

        // Act & Assert
        logger.info("TC009 - Output: exceptionThrown={}", true);
        
        assertThatThrownBy(() -> facultyService.deleteFaculty("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(facultyRepository, never()).deleteById(anyString());
    }

    @Test
    @DisplayName("DT010 - Create faculty with long name")
    void testCreateFacultyWithLongName() {
        // Arrange
        FacultyRequest request = new FacultyRequest();
        request.setFacultyName("Trường Đại học Công nghệ thông tin - Công nghệ thông tin Hệ thống");

        Faculty newFaculty = new Faculty();
        newFaculty.setId(facultyId1);
        newFaculty.setFacultyName(request.getFacultyName());

        when(facultyRepository.save(any(Faculty.class))).thenReturn(newFaculty);
        logger.info("TC010 - Input: longFacultyName={}", request.getFacultyName());

        // Act
        FacultyResponse response = facultyService.createFaculty(request);

        // Assert
        logger.info("TC010 - Output: savedName={}", response.getFacultyName());
        
        assertThat(response.getFacultyName()).isEqualTo(request.getFacultyName());
    }
}

