package ca.hanson.shiftflow_backend.controller;

import ca.hanson.shiftflow_backend.dto.CreateOpenShiftOfferRequest;
import ca.hanson.shiftflow_backend.dto.OpenShiftOfferResponse;
import ca.hanson.shiftflow_backend.service.OpenShiftOfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/open-shifts")
public class OpenShiftOfferController {

    private final OpenShiftOfferService openShiftOfferService;

    @Autowired
    public OpenShiftOfferController(OpenShiftOfferService openShiftOfferService) {
        this.openShiftOfferService = openShiftOfferService;
    }

    @PostMapping
    public ResponseEntity<OpenShiftOfferResponse> createOffer(
            @RequestBody CreateOpenShiftOfferRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(openShiftOfferService.createOffer(request, authentication));
    }

    @GetMapping
    public ResponseEntity<List<OpenShiftOfferResponse>> getOpenOffers() {
        return ResponseEntity.ok(openShiftOfferService.getOpenOffers());
    }

    @GetMapping("/me")
    public ResponseEntity<List<OpenShiftOfferResponse>> getMyOffers(Authentication authentication) {
        return ResponseEntity.ok(openShiftOfferService.getMyOffers(authentication));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<OpenShiftOfferResponse> cancelOffer(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(openShiftOfferService.cancelOffer(id, authentication));
    }

    @PutMapping("/{id}/claim")
    public ResponseEntity<OpenShiftOfferResponse> claimOffer(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(openShiftOfferService.claimOffer(id, authentication));
    }
}