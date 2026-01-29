package ca.hanson.shiftflow_backend.dto;

import java.time.LocalDateTime;

public record ShiftResponse(
        Long id,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String position,
        String location,
        UserSummaryResponse assignedEmployee,
        UserSummaryResponse createdBy,
        LocalDateTime createdAt
) {
}
