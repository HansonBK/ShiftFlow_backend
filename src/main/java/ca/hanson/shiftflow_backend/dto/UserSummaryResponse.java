package ca.hanson.shiftflow_backend.dto;

public record UserSummaryResponse(
        Long id,
        String firstName,
        String lastName
) {
}
