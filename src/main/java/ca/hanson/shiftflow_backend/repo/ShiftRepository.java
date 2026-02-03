package ca.hanson.shiftflow_backend.repo;

import ca.hanson.shiftflow_backend.entitiy.Shift;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {

    List<Shift> findByAssignedEmployeeId(Long assignedEmployeeId);

    List<Shift> findByAssignedEmployeeEmail(String email);

    List<Shift> findByAssignedEmployeeIdAndCreatedByEmailOrderByStartTimeAsc(
            Long assignedEmployeeId,
            String managerEmail
    );

    List<Shift> findByAssignedEmployeeEmailOrderByStartTimeAsc(String email);

}
