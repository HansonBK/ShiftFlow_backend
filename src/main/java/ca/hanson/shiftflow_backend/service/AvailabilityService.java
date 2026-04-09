package ca.hanson.shiftflow_backend.service;

import ca.hanson.shiftflow_backend.dto.AvailabilityRequest;
import ca.hanson.shiftflow_backend.dto.AvailabilitySlotResponse;
import ca.hanson.shiftflow_backend.dto.UserSummaryResponse;
import ca.hanson.shiftflow_backend.entitiy.Availability;
import ca.hanson.shiftflow_backend.entitiy.Role;
import ca.hanson.shiftflow_backend.entitiy.User;
import ca.hanson.shiftflow_backend.repo.AvailabilityRepository;
import ca.hanson.shiftflow_backend.repo.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Service
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;

    public AvailabilityService(AvailabilityRepository availabilityRepository, UserRepository userRepository) {
        this.availabilityRepository = availabilityRepository;
        this.userRepository = userRepository;
    }

    public AvailabilitySlotResponse createAvailability(AvailabilityRequest request, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        validateRequest(request);

        ensureNoOverlap(currentUser.getId(), request.dayOfWeek(), request.startTime(), request.endTime(), null);

        Availability availability = new Availability();
        availability.setUser(currentUser);
        availability.setDayOfWeek(request.dayOfWeek());
        availability.setStartTime(request.startTime());
        availability.setEndTime(request.endTime());

        Availability saved = availabilityRepository.save(availability);
        return toResponse(saved);
    }

    public AvailabilitySlotResponse updateAvailability(Long id, AvailabilityRequest request, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "availability id is required");
        }
        validateRequest(request);

        Availability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Availability not found"));

        if (!availability.getUser().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to update this availability");
        }

        ensureNoOverlap(availability.getUser().getId(), request.dayOfWeek(), request.startTime(), request.endTime(), id);

        availability.setDayOfWeek(request.dayOfWeek());
        availability.setStartTime(request.startTime());
        availability.setEndTime(request.endTime());

        Availability saved = availabilityRepository.save(availability);
        return toResponse(saved);
    }

    public void deleteAvailability(Long id, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "availability id is required");
        }

        Availability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Availability not found"));

        if (!availability.getUser().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this availability");
        }

        availabilityRepository.delete(availability);
    }

    public List<AvailabilitySlotResponse> getMyAvailability(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        return availabilityRepository.findByUserIdOrderByDayOfWeekAscStartTimeAsc(currentUser.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AvailabilitySlotResponse> getAvailabilityForEmployee(Long employeeId, Authentication authentication) {
        getCurrentUser(authentication);
        if (employeeId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "employee id is required");
        }

        userRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        return availabilityRepository.findByUserIdOrderByDayOfWeekAscStartTimeAsc(employeeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateRequest(AvailabilityRequest request) {
        if (request == null || request.dayOfWeek() == null || request.startTime() == null || request.endTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "day, start time, and end time are required");
        }
        if (!request.startTime().isBefore(request.endTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "start time must be before end time");
        }
    }

    private void ensureNoOverlap(Long userId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, Long excludeId) {
        boolean exists = (excludeId == null)
                ? availabilityRepository.existsByUserIdAndDayOfWeekAndStartTimeLessThanAndEndTimeGreaterThan(
                        userId, dayOfWeek, endTime, startTime)
                : availabilityRepository.existsByUserIdAndDayOfWeekAndIdNotAndStartTimeLessThanAndEndTimeGreaterThan(
                        userId, dayOfWeek, excludeId, endTime, startTime);

        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Availability overlaps an existing time range");
        }
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        String email = (String) authentication.getPrincipal();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private AvailabilitySlotResponse toResponse(Availability availability) {
        return new AvailabilitySlotResponse(
                availability.getId(),
                toUserSummary(availability.getUser()),
                availability.getDayOfWeek(),
                availability.getStartTime(),
                availability.getEndTime(),
                availability.getCreatedAt()
        );
    }

    private UserSummaryResponse toUserSummary(User user) {
        return new UserSummaryResponse(user.getId(), user.getFirstName(), user.getLastName());
    }
}
