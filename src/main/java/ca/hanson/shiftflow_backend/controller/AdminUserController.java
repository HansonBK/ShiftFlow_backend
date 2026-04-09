package ca.hanson.shiftflow_backend.controller;

import ca.hanson.shiftflow_backend.dto.CreateUserRequest;
import ca.hanson.shiftflow_backend.entity.User;
import ca.hanson.shiftflow_backend.service.AdminUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {

        User created = adminUserService.createUser(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<User> deleteUser(@PathVariable Long id) {

        adminUserService.deleteUser(id);
        return ResponseEntity.noContent().build();

    }

    @GetMapping()
    public List<User> getAllUsers() {

        return adminUserService.getAllUsers();
    }

}
