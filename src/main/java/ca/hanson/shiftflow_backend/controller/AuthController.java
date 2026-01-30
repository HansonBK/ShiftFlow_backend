package ca.hanson.shiftflow_backend.controller;

import ca.hanson.shiftflow_backend.dto.LoginRequest;
import ca.hanson.shiftflow_backend.dto.LoginResponse;
import ca.hanson.shiftflow_backend.dto.MeResponse;
import ca.hanson.shiftflow_backend.service.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;


    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;

    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(Authentication authentication) {

        return ResponseEntity.ok(authService.getMe(authentication));
    }




}
