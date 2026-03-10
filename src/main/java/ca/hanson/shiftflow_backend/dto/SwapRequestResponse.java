package ca.hanson.shiftflow_backend.dto;

import ca.hanson.shiftflow_backend.entitiy.SwapRequestStatus;

import java.time.LocalDateTime;

public record SwapRequestResponse(
        Long id,
        SwapRequestStatus status,
        UserSummaryResponse requester,
        UserSummaryResponse targetUser,
        SwapShiftSummaryResponse requesterShift,
        SwapShiftSummaryResponse targetShift,
        LocalDateTime createdAt,
        LocalDateTime respondedAt
) {
}