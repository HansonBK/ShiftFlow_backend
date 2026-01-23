package ca.hanson.shiftflow_backend.dto;


public record LoginResponse(String firstName, String lastName, String email, String role, String token) {


}
