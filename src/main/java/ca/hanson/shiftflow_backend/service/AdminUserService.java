package ca.hanson.shiftflow_backend.service;

import ca.hanson.shiftflow_backend.dto.CreateUserRequest;
import ca.hanson.shiftflow_backend.entitiy.Role;
import ca.hanson.shiftflow_backend.entitiy.User;
import ca.hanson.shiftflow_backend.repo.UserRepository;


import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;


@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(CreateUserRequest request) {

        if (request == null || request.email().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        if ( request.password() == null ||request.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }

        boolean exists = userRepository.findByEmail(request.email()).isPresent();
        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
       Role roleEnum;


        try{

            roleEnum = Role.valueOf(request.role().toUpperCase());

        }catch(IllegalArgumentException e){

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid role");
        }

        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(roleEnum);


        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);


    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
    }
}
