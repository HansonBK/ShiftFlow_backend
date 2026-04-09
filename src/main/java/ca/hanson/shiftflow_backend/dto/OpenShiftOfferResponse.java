package ca.hanson.shiftflow_backend.dto;

import ca.hanson.shiftflow_backend.entity.OpenShiftStatus;

import java.time.LocalDateTime;

public record OpenShiftOfferResponse(
        Long id,
        OpenShiftStatus status,
        Long shiftId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String position,
        String location,
        UserSummaryResponse createdBy,
        UserSummaryResponse claimedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}