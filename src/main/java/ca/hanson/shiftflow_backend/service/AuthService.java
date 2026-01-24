package ca.hanson.shiftflow_backend.service;

import ca.hanson.shiftflow_backend.dto.LoginRequest;
import ca.hanson.shiftflow_backend.dto.LoginResponse;
import ca.hanson.shiftflow_backend.entitiy.User;
import ca.hanson.shiftflow_backend.repo.UserRepository;
import ca.hanson.shiftflow_backend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.function.EntityResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,  JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

        this.jwtTokenProvider = jwtTokenProvider;
    }



    public LoginResponse login(LoginRequest loginRequest) {

        User user  =userRepository.findByEmail(loginRequest.email());


        if(user == null) {

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");

        }
        if(!passwordEncoder.matches(loginRequest.password(), user.getPasswordHash())) {

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");

        }
        String role = user.getRole().name();
        String token = jwtTokenProvider.generateToken(loginRequest.email(), role);
        return new LoginResponse(
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                role,
                token
        );



    }


}
