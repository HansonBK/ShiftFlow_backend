package ca.hanson.shiftflow_backend.service;

import ca.hanson.shiftflow_backend.dto.CreateShiftRequest;
import ca.hanson.shiftflow_backend.dto.ShiftResponse;
import ca.hanson.shiftflow_backend.dto.UserSummaryResponse;
import ca.hanson.shiftflow_backend.entitiy.Shift;
import ca.hanson.shiftflow_backend.entitiy.User;
import ca.hanson.shiftflow_backend.repo.ShiftRepository;
import ca.hanson.shiftflow_backend.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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


        if(authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        if(request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST , "request cannot be empty");
        }

        if(request.startTime()==null || request.endTime()==null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST , "start time or end time cannot be empty");
        }

        if(!request.startTime().isBefore(request.endTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST , "start time must be before end time");
        }

        String managerEmail = (String) authentication.getPrincipal();

        User manager = userRepository.findByEmail(managerEmail)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Manager user not found"));

        User assignedEmployee = null;
        if(request.assignedEmployeeId() != null){
            assignedEmployee = userRepository.findById(request.assignedEmployeeId())
                    .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        }

        Shift shift = new Shift();
        shift.setStartTime(request.startTime());
        shift.setEndTime(request.endTime());
        shift.setPosition(request.position());
        shift.setLocation(request.location());
        shift.setCreatedBy(manager);
        shift.setAssignedEmployee(assignedEmployee);

        Shift saved = shiftRepository.save(shift);

        return  toShiftResponse(saved);




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


}
