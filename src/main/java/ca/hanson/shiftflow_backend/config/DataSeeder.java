package ca.hanson.shiftflow_backend.config;

import ca.hanson.shiftflow_backend.entity.Role;
import ca.hanson.shiftflow_backend.entity.User;
import ca.hanson.shiftflow_backend.repo.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
@Order(Ordered.LOWEST_PRECEDENCE)
public class DataSeeder implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public DataSeeder(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }



    @Override
    public void run(String... args) {
        System.out.println("✅ DataSeeder running...");

        String adminEmail = "admin@shiftflow.com";

        if (userRepository.findByEmail(adminEmail).isPresent()) return;


        User admin = new User();
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setEmail(adminEmail);
        admin.setPasswordHash(passwordEncoder.encode("admin"));
        admin.setRole(Role.ADMIN);

        userRepository.save(admin);
    }

}
