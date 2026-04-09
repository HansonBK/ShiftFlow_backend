package ca.hanson.shiftflow_backend.repo;

import ca.hanson.shiftflow_backend.entity.User;
import ca.hanson.shiftflow_backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByRole(Role role);

}
