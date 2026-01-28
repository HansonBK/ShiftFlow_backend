package ca.hanson.shiftflow_backend.dto;



public record CreateUserRequest(
        String firstName,
        String lastName,
        String email,
        String password,
        String role
) { }

