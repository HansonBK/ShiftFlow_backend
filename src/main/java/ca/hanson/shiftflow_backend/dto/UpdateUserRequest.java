package ca.hanson.shiftflow_backend.dto;

public record UpdateUserRequest(
        String firstName,
        String lastName,
        String role
) { }
