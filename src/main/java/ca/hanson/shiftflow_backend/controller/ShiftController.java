package ca.hanson.shiftflow_backend.controller;


import ca.hanson.shiftflow_backend.dto.CreateShiftRequest;
import ca.hanson.shiftflow_backend.dto.ShiftResponse;
import ca.hanson.shiftflow_backend.dto.UpdateShiftRequest;
import ca.hanson.shiftflow_backend.service.ShiftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shifts")
public class ShiftController {

    private final ShiftService shiftService;

    @Autowired
    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @PostMapping()
    public ResponseEntity<ShiftResponse> createShift(@RequestBody CreateShiftRequest request, Authentication authentication) {

        ShiftResponse response = shiftService.createShift(request, authentication);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShift(@PathVariable Long id, Authentication authentication) {
        shiftService.deleteShift(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShiftResponse> updateShift(@PathVariable Long id, @RequestBody UpdateShiftRequest request, Authentication authentication){

        ShiftResponse response = shiftService.updateShift(id, request, authentication);

        return ResponseEntity.ok(response);

    }

    @GetMapping("/all")
    public ResponseEntity<List<ShiftResponse>> getAllShifts(Authentication authentication) {
        List<ShiftResponse> shifts = shiftService.getShifts(authentication);
        return ResponseEntity.ok(shifts);
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<ShiftResponse>> getShiftsForEmployee(
            @PathVariable Long employeeId,
            Authentication authentication
    ) {
        List<ShiftResponse> shifts = shiftService.getShiftsForEmployee(employeeId, authentication);
        return ResponseEntity.ok(shifts);
    }

    @GetMapping("/me")
    public ResponseEntity<List<ShiftResponse>> getMyShifts(Authentication authentication) {
        List<ShiftResponse> shifts = shiftService.getMyShifts(authentication);
        return ResponseEntity.ok(shifts);
    }

}
