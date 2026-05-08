package com.ptit.schedule.dto;

import com.ptit.schedule.entity.RoomStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomStatusUpdateRequest {

    @NotNull(message = "Trạng thái phòng không được để trống")
    private RoomStatus status;
}
