package ca.hanson.shiftflow_backend.repo;

import ca.hanson.shiftflow_backend.entity.Shift;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {

    List<Shift> findByAssignedEmployeeIdAndCreatedByEmailOrderByStartTimeAsc(Long assignedEmployeeId, String createdByEmail);

    List<Shift> findByAssignedEmployeeEmailOrderByStartTimeAsc(String email);

    boolean existsByAssignedEmployeeIdAndStartTimeLessThanAndEndTimeGreaterThan(Long employeeId, LocalDateTime endTime, LocalDateTime startTime);

    boolean existsByAssignedEmployeeIdAndIdNotAndStartTimeLessThanAndEndTimeGreaterThan(Long employeeId, Long excludeShiftId, LocalDateTime endTime, LocalDateTime startTime);

}
