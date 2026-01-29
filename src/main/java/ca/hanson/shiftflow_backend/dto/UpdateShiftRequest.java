package ca.hanson.shiftflow_backend.dto;

import java.time.LocalDateTime;

public record UpdateShiftRequest(LocalDateTime startTime,
                                 LocalDateTime endTime,
                                 String position,
                                 String location,
                                 Long assignedEmployeeId,
                                 Boolean clearAssignedEmployee) {
}
