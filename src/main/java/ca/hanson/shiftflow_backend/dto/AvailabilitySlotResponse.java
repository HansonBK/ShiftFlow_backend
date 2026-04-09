package ca.hanson.shiftflow_backend.dto;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AvailabilitySlotResponse(
        Long id,
        UserSummaryResponse user,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        LocalDateTime createdAt
) {
}
