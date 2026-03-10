package ca.hanson.shiftflow_backend.service;

import ca.hanson.shiftflow_backend.dto.CreateSwapRequest;
import ca.hanson.shiftflow_backend.dto.SwapRequestResponse;
import ca.hanson.shiftflow_backend.dto.SwapShiftSummaryResponse;
import ca.hanson.shiftflow_backend.dto.UserSummaryResponse;
import ca.hanson.shiftflow_backend.entitiy.Shift;
import ca.hanson.shiftflow_backend.entitiy.SwapRequest;
import ca.hanson.shiftflow_backend.entitiy.SwapRequestStatus;
import ca.hanson.shiftflow_backend.entitiy.User;
import ca.hanson.shiftflow_backend.repo.ShiftRepository;
import ca.hanson.shiftflow_backend.repo.SwapRequestRepository;
import ca.hanson.shiftflow_backend.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class SwapRequestService {

    private final SwapRequestRepository swapRequestRepository;
    private final ShiftRepository shiftRepository;
    private final UserRepository userRepository;

    @Autowired
    public SwapRequestService(
            SwapRequestRepository swapRequestRepository,
            ShiftRepository shiftRepository,
            UserRepository userRepository
    ) {
        this.swapRequestRepository = swapRequestRepository;
        this.shiftRepository = shiftRepository;
        this.userRepository = userRepository;
    }

    public SwapRequestResponse createSwapRequest(CreateSwapRequest request, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        if (request == null || request.myShiftId() == null || request.targetShiftId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "myShiftId and targetShiftId are required");
        }

        if (request.myShiftId().equals(request.targetShiftId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cannot request swap with the same shift");
        }

        String requesterEmail = (String) authentication.getPrincipal();

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Shift myShift = shiftRepository.findById(request.myShiftId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Your shift was not found"));

        Shift targetShift = shiftRepository.findById(request.targetShiftId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target shift was not found"));

        if (myShift.getAssignedEmployee() == null || !myShift.getAssignedEmployee().getId().equals(requester.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only swap your own shift");
        }

        if (targetShift.getAssignedEmployee() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target shift must already be assigned to another employee");
        }

        if (targetShift.getAssignedEmployee().getId().equals(requester.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot swap with your own other shift");
        }

        if (myShift.getStartTime().isBefore(LocalDateTime.now()) || targetShift.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Past shifts cannot be used for swap requests");
        }

        Set<SwapRequestStatus> activeStatuses = Set.of(
                SwapRequestStatus.PENDING,
                SwapRequestStatus.ACCEPTED_BY_TARGET
        );

        boolean myShiftBusy = swapRequestRepository.existsByRequesterShiftIdAndStatusIn(myShift.getId(), activeStatuses)
                || swapRequestRepository.existsByTargetShiftIdAndStatusIn(myShift.getId(), activeStatuses);

        boolean targetShiftBusy = swapRequestRepository.existsByRequesterShiftIdAndStatusIn(targetShift.getId(), activeStatuses)
                || swapRequestRepository.existsByTargetShiftIdAndStatusIn(targetShift.getId(), activeStatuses);

        if (myShiftBusy || targetShiftBusy) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "One of these shifts already has an active swap request");
        }

        SwapRequest swapRequest = new SwapRequest();
        swapRequest.setRequester(requester);
        swapRequest.setTargetUser(targetShift.getAssignedEmployee());
        swapRequest.setRequesterShift(myShift);
        swapRequest.setTargetShift(targetShift);
        swapRequest.setStatus(SwapRequestStatus.PENDING);

        SwapRequest saved = swapRequestRepository.save(swapRequest);

        return toResponse(saved);
    }

    public List<SwapRequestResponse> getInbox(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String email = (String) authentication.getPrincipal();

        return swapRequestRepository.findByTargetUserEmailOrderByCreatedAtDesc(email)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<SwapRequestResponse> getSent(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String email = (String) authentication.getPrincipal();

        return swapRequestRepository.findByRequesterEmailOrderByCreatedAtDesc(email)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SwapRequestResponse accept(Long requestId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String email = (String) authentication.getPrincipal();

        SwapRequest swapRequest = swapRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Swap request not found"));

        if (!swapRequest.getTargetUser().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only accept requests sent to you");
        }

        if (swapRequest.getStatus() != SwapRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending requests can be accepted");
        }

        swapRequest.setStatus(SwapRequestStatus.ACCEPTED_BY_TARGET);
        swapRequest.setRespondedAt(LocalDateTime.now());

        SwapRequest saved = swapRequestRepository.save(swapRequest);

        return toResponse(saved);
    }

    public SwapRequestResponse decline(Long requestId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String email = (String) authentication.getPrincipal();

        SwapRequest swapRequest = swapRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Swap request not found"));

        if (!swapRequest.getTargetUser().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only decline requests sent to you");
        }

        if (swapRequest.getStatus() != SwapRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending requests can be declined");
        }

        swapRequest.setStatus(SwapRequestStatus.DECLINED_BY_TARGET);
        swapRequest.setRespondedAt(LocalDateTime.now());

        SwapRequest saved = swapRequestRepository.save(swapRequest);

        return toResponse(saved);
    }

    private SwapRequestResponse toResponse(SwapRequest swapRequest) {
        return new SwapRequestResponse(
                swapRequest.getId(),
                swapRequest.getStatus(),
                toUserSummary(swapRequest.getRequester()),
                toUserSummary(swapRequest.getTargetUser()),
                toShiftSummary(swapRequest.getRequesterShift()),
                toShiftSummary(swapRequest.getTargetShift()),
                swapRequest.getCreatedAt(),
                swapRequest.getRespondedAt()
        );
    }

    private SwapShiftSummaryResponse toShiftSummary(Shift shift) {
        return new SwapShiftSummaryResponse(
                shift.getId(),
                shift.getStartTime(),
                shift.getEndTime(),
                shift.getPosition(),
                shift.getLocation(),
                toUserSummary(shift.getAssignedEmployee())
        );
    }

    private UserSummaryResponse toUserSummary(User user) {
        if (user == null) return null;
        return new UserSummaryResponse(user.getId(), user.getFirstName(), user.getLastName());
    }
}