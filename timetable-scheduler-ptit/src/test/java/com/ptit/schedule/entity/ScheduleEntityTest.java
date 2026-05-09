package com.ptit.schedule.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduleEntityTest {

    @Test
    @DisplayName("LL-ENT-01: Schedule builder/getters/setters/toString/equals/hashCode")
    void scheduleEntity_shouldSupportLombokGeneratedMethods() {
        Schedule s1 = Schedule.builder()
                .id(1L)
                .classNumber(1)
                .studentYear("2024")
                .major("CNTT")
                .specialSystem("CLC")
                .siSoMotLop(50)
                .build();

        Schedule s2 = Schedule.builder()
                .id(1L)
                .classNumber(1)
                .studentYear("2024")
                .major("CNTT")
                .specialSystem("CLC")
                .siSoMotLop(50)
                .build();

        assertThat(s1.getId()).isEqualTo(1L);
        s1.setMajor("CNTT2");
        assertThat(s1.getMajor()).isEqualTo("CNTT2");

        // basic equals/hashCode/toString should be callable
        assertThat(s1.toString()).contains("Schedule");
        assertThat(s1.hashCode()).isNotZero();
        assertThat(s1.equals(s2)).isFalse(); // major changed
    }
}

