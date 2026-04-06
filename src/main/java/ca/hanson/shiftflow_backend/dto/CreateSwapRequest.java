package ca.hanson.shiftflow_backend.dto;

public record CreateSwapRequest(
        Long myShiftId,
        Long targetShiftId
) {
}