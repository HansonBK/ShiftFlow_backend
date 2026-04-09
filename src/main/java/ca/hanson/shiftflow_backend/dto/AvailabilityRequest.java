package ca.hanson.shiftflow_backend.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record AvailabilityRequest(
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
) {
}
