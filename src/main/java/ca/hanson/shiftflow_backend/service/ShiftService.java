package ca.hanson.shiftflow_backend.service;

import ca.hanson.shiftflow_backend.dto.CreateShiftRequest;
import ca.hanson.shiftflow_backend.dto.ShiftResponse;
import ca.hanson.shiftflow_backend.dto.UpdateShiftRequest;
import ca.hanson.shiftflow_backend.dto.UserSummaryResponse;
import ca.hanson.shiftflow_backend.entity.Shift;
import ca.hanson.shiftflow_backend.entity.User;
import ca.hanson.shiftflow_backend.repo.ShiftRepository;
import ca.hanson.shiftflow_backend.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final UserRepository userRepository;

    @Autowired
    public ShiftService(ShiftRepository shiftRepository, UserRepository userRepository) {
        this.shiftRepository = shiftRepository;
        this.userRepository = userRepository;
    }


    public ShiftResponse createShift(CreateShiftRequest request, Authentication authentication) {


        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "request cannot be empty");
        }

        if (request.startTime() == null || request.endTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "start time or end time cannot be empty");
        }

        if (!request.startTime().isBefore(request.endTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "start time must be before end time");
        }

        String managerEmail = (String) authentication.getPrincipal();

        User manager = userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Manager user not found"));

        User assignedEmployee = null;
        if (request.assignedEmployeeId() != null) {
            assignedEmployee = userRepository.findById(request.assignedEmployeeId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        }

        if (assignedEmployee != null) {
            ensureNoOverlap(assignedEmployee.getId(), request.startTime(), request.endTime(), null);
        }

        Shift shift = new Shift();
        shift.setStartTime(request.startTime());
        shift.setEndTime(request.endTime());
        shift.setPosition(request.position());
        shift.setLocation(request.location());
        shift.setCreatedBy(manager);
        shift.setAssignedEmployee(assignedEmployee);

        Shift saved = shiftRepository.save(shift);

        return toShiftResponse(saved);


    }

    private ShiftResponse toShiftResponse(Shift shift) {
        return new ShiftResponse(
                shift.getId(),
                shift.getStartTime(),
                shift.getEndTime(),
                shift.getPosition(),
                shift.getLocation(),
                toUserSummary(shift.getAssignedEmployee()),
                toUserSummary(shift.getCreatedBy()),
                shift.getCreatedAt()
        );
    }

    private UserSummaryResponse toUserSummary(User user) {
        if (user == null) return null;
        return new UserSummaryResponse(user.getId(), user.getFirstName(), user.getLastName());
    }


    public void deleteShift(Long id, Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String managerEmail = (String) authentication.getPrincipal();

        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Shift not found"
                ));


        if (!shift.getCreatedBy().getEmail().equals(managerEmail)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You are not allowed to delete this shift"
            );
        }

        shiftRepository.delete(shift);
    }


    public ShiftResponse updateShift(Long id, UpdateShiftRequest request, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "request cannot be empty");
        }
        String managerEmail = (String) authentication.getPrincipal();

        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shift not found"));

        if (shift.getCreatedBy() == null || !shift.getCreatedBy().getEmail().equals(managerEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to update this shift");
        }

        LocalDateTime newStartTime = (request.startTime() != null) ? request.startTime() : shift.getStartTime();
        LocalDateTime newEndTime = (request.endTime() != null) ? request.endTime() : shift.getEndTime();

        if (newStartTime == null || newEndTime == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "start time or end time cannot be empty");
        }
        if (!newStartTime.isBefore(newEndTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "start time must be before end time");
        }
        shift.setStartTime(newStartTime);
        shift.setEndTime(newEndTime);

        if (request.position() != null) shift.setPosition(request.position());
        if (request.location() != null) shift.setLocation(request.location());

        boolean wantClear = Boolean.TRUE.equals(request.clearAssignedEmployee());
        boolean wantAssignedEmployee = request.assignedEmployeeId() != null;

        if (wantClear && wantAssignedEmployee) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cannot assign and clear assigned employee at the same time");
        }
        if (wantClear) {
            shift.setAssignedEmployee(null);

        } else if (wantAssignedEmployee) {
            User employee = userRepository.findById(request.assignedEmployeeId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
            ensureNoOverlap(employee.getId(), newStartTime, newEndTime, shift.getId());
            shift.setAssignedEmployee(employee);
        } else if (shift.getAssignedEmployee() != null) {
            ensureNoOverlap(shift.getAssignedEmployee().getId(), newStartTime, newEndTime, shift.getId());
        }
        Shift saved = shiftRepository.save(shift);
        return toShiftResponse(saved);

    }

    public List<ShiftResponse> getShifts(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return shiftRepository.findAll()
                .stream()
                .map(this::toShiftResponse)
                .toList();
    }

    public List<ShiftResponse> getShiftsForEmployee(Long employeeId, Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        if (employeeId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "employee id cannot be empty");
        }

        userRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        String managerEmail = (String) authentication.getPrincipal();

        return shiftRepository.findByAssignedEmployeeIdAndCreatedByEmailOrderByStartTimeAsc(
                        employeeId,
                        managerEmail
                )
                .stream()
                .map(this::toShiftResponse)
                .toList();
    }

    public List<ShiftResponse> getMyShifts(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String email = (String) authentication.getPrincipal();

        return shiftRepository.findByAssignedEmployeeEmailOrderByStartTimeAsc(email)
                .stream()
                .map(this::toShiftResponse)
                .toList();
    }

    private void ensureNoOverlap(Long employeeId, LocalDateTime startTime, LocalDateTime endTime, Long excludeShiftId) {
        if (employeeId == null || startTime == null || endTime == null) {
            return;
        }

        boolean exists = (excludeShiftId == null)
                ? shiftRepository.existsByAssignedEmployeeIdAndStartTimeLessThanAndEndTimeGreaterThan(
                employeeId, endTime, startTime)
                : shiftRepository.existsByAssignedEmployeeIdAndIdNotAndStartTimeLessThanAndEndTimeGreaterThan(
                employeeId, excludeShiftId, endTime, startTime);

        if (exists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Employee already has a shift that overlaps this time range"
            );
        }
    }

    public void validateNoOverlap(Long employeeId, LocalDateTime startTime, LocalDateTime endTime, Long excludeShiftId) {
        ensureNoOverlap(employeeId, startTime, endTime, excludeShiftId);
    }


}
