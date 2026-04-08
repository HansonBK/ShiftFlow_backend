package ca.hanson.shiftflow_backend.controller;

import ca.hanson.shiftflow_backend.dto.AvailabilityRequest;
import ca.hanson.shiftflow_backend.dto.AvailabilitySlotResponse;
import ca.hanson.shiftflow_backend.service.AvailabilityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PostMapping
    public ResponseEntity<AvailabilitySlotResponse> createAvailability(
            @RequestBody AvailabilityRequest request,
            Authentication authentication
    ) {
        AvailabilitySlotResponse response = availabilityService.createAvailability(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AvailabilitySlotResponse> updateAvailability(
            @PathVariable Long id,
            @RequestBody AvailabilityRequest request,
            Authentication authentication
    ) {
        AvailabilitySlotResponse response = availabilityService.updateAvailability(id, request, authentication);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAvailability(@PathVariable Long id, Authentication authentication) {
        availabilityService.deleteAvailability(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<List<AvailabilitySlotResponse>> getMyAvailability(Authentication authentication) {
        List<AvailabilitySlotResponse> availability = availabilityService.getMyAvailability(authentication);
        return ResponseEntity.ok(availability);
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<AvailabilitySlotResponse>> getAvailabilityForEmployee(
            @PathVariable Long employeeId,
            Authentication authentication
    ) {
        List<AvailabilitySlotResponse> availability = availabilityService.getAvailabilityForEmployee(employeeId, authentication);
        return ResponseEntity.ok(availability);
    }
}
