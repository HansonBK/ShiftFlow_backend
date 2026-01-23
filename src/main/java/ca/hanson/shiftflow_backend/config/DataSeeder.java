package ca.hanson.shiftflow_backend.config;

import ca.hanson.shiftflow_backend.entitiy.Role;
import ca.hanson.shiftflow_backend.entitiy.User;
import ca.hanson.shiftflow_backend.repo.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class DataSeeder implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public DataSeeder(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {

        String adminEmail = "admin@shiftflow.com";

        if(userRepository.findByEmail(adminEmail) != null){
            return;
        }
        User admin = new User();
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setEmail(adminEmail);
        admin.setPasswordHash(passwordEncoder.encode("admin"));
        admin.setRole(Role.ADMIN);
        admin.setCreatedAt(LocalDateTime.now());

        userRepository.save(admin);



    }

}
