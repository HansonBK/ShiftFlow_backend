package ca.hanson.shiftflow_backend.dto;

import java.time.LocalDateTime;

public record SwapShiftSummaryResponse(
        Long id,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String position,
        String location,
        UserSummaryResponse assignedEmployee
) {
}