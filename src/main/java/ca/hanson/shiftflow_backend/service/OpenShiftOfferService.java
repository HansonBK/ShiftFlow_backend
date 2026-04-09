package ca.hanson.shiftflow_backend.service;

import ca.hanson.shiftflow_backend.dto.CreateOpenShiftOfferRequest;
import ca.hanson.shiftflow_backend.dto.OpenShiftOfferResponse;
import ca.hanson.shiftflow_backend.dto.UserSummaryResponse;
import ca.hanson.shiftflow_backend.entity.OpenShiftOffer;
import ca.hanson.shiftflow_backend.entity.OpenShiftStatus;
import ca.hanson.shiftflow_backend.entity.Shift;
import ca.hanson.shiftflow_backend.entity.User;
import ca.hanson.shiftflow_backend.repo.OpenShiftOfferRepository;
import ca.hanson.shiftflow_backend.repo.ShiftRepository;
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
public class OpenShiftOfferService {

    private final OpenShiftOfferRepository openShiftOfferRepository;
    private final ShiftRepository shiftRepository;
    private final UserRepository userRepository;

    @Autowired
    public OpenShiftOfferService(
            OpenShiftOfferRepository openShiftOfferRepository,
            ShiftRepository shiftRepository,
            UserRepository userRepository
    ) {
        this.openShiftOfferRepository = openShiftOfferRepository;
        this.shiftRepository = shiftRepository;
        this.userRepository = userRepository;
    }

    public OpenShiftOfferResponse createOffer(CreateOpenShiftOfferRequest request, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        if (request == null || request.shiftId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "shiftId is required");
        }

        String email = (String) authentication.getPrincipal();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Shift shift = shiftRepository.findById(request.shiftId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shift not found"));

        if (shift.getAssignedEmployee() == null || !shift.getAssignedEmployee().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only offer your own shift");
        }

        if (shift.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Past shifts cannot be offered");
        }

        boolean alreadyOpen = openShiftOfferRepository.existsByShiftIdAndStatusIn(
                shift.getId(),
                Set.of(OpenShiftStatus.OPEN, OpenShiftStatus.CLAIMED)
        );

        if (alreadyOpen) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This shift already has an active open offer");
        }

        OpenShiftOffer offer = new OpenShiftOffer();
        offer.setShift(shift);
        offer.setCreatedBy(user);
        offer.setStatus(OpenShiftStatus.OPEN);

        return toResponse(openShiftOfferRepository.save(offer));
    }

    public List<OpenShiftOfferResponse> getOpenOffers() {
        return openShiftOfferRepository.findByStatusOrderByCreatedAtDesc(OpenShiftStatus.OPEN)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<OpenShiftOfferResponse> getMyOffers(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String email = (String) authentication.getPrincipal();

        return openShiftOfferRepository.findByCreatedByEmailOrderByCreatedAtDesc(email)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public OpenShiftOfferResponse cancelOffer(Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String email = (String) authentication.getPrincipal();

        OpenShiftOffer offer = openShiftOfferRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Open shift offer not found"));

        if (!offer.getCreatedBy().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only cancel your own open shift offer");
        }

        if (offer.getStatus() != OpenShiftStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only open offers can be cancelled");
        }

        offer.setStatus(OpenShiftStatus.CANCELLED);

        return toResponse(openShiftOfferRepository.save(offer));
    }

    public OpenShiftOfferResponse claimOffer(Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String email = (String) authentication.getPrincipal();

        User claimer = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        OpenShiftOffer offer = openShiftOfferRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Open shift offer not found"));

        if (offer.getCreatedBy().getId().equals(claimer.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot claim your own shift offer");
        }

        if (offer.getStatus() != OpenShiftStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This shift offer is no longer available");
        }

        Shift shift = offer.getShift();
        shift.setAssignedEmployee(claimer);
        shiftRepository.save(shift);

        offer.setClaimedBy(claimer);
        offer.setStatus(OpenShiftStatus.CLAIMED);

        return toResponse(openShiftOfferRepository.save(offer));
    }

    private OpenShiftOfferResponse toResponse(OpenShiftOffer offer) {
        return new OpenShiftOfferResponse(
                offer.getId(),
                offer.getStatus(),
                offer.getShift().getId(),
                offer.getShift().getStartTime(),
                offer.getShift().getEndTime(),
                offer.getShift().getPosition(),
                offer.getShift().getLocation(),
                toUserSummary(offer.getCreatedBy()),
                toUserSummary(offer.getClaimedBy()),
                offer.getCreatedAt(),
                offer.getUpdatedAt()
        );
    }

    private UserSummaryResponse toUserSummary(User user) {
        if (user == null) return null;
        return new UserSummaryResponse(user.getId(), user.getFirstName(), user.getLastName());
    }
}