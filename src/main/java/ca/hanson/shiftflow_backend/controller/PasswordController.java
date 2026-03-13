package ca.hanson.shiftflow_backend.controller;

import ca.hanson.shiftflow_backend.dto.ChangePasswordRequest;
import ca.hanson.shiftflow_backend.service.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/password")
public class PasswordController {

    private final PasswordService passwordService;

    @Autowired
    public PasswordController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @PutMapping("/change")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {
        String message = passwordService.changePassword(request, authentication);
        return ResponseEntity.ok(Map.of("message", message));
    }
}