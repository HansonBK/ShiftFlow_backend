package ca.hanson.shiftflow_backend.dto;

public record ChangePasswordRequest(
        String currentPassword,
        String newPassword
) {
}