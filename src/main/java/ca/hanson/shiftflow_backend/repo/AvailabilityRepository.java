package ca.hanson.shiftflow_backend.repo;

import ca.hanson.shiftflow_backend.entitiy.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    List<Availability> findByUserIdOrderByDayOfWeekAscStartTimeAsc(Long userId);

    boolean existsByUserIdAndDayOfWeekAndStartTimeLessThanAndEndTimeGreaterThan(
            Long userId,
            DayOfWeek dayOfWeek,
            LocalTime newEndTime,
            LocalTime newStartTime
    );

    boolean existsByUserIdAndDayOfWeekAndIdNotAndStartTimeLessThanAndEndTimeGreaterThan(
            Long userId,
            DayOfWeek dayOfWeek,
            Long id,
            LocalTime newEndTime,
            LocalTime newStartTime
    );
}
