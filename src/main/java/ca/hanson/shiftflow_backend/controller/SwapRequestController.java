package ca.hanson.shiftflow_backend.controller;

import ca.hanson.shiftflow_backend.dto.CreateSwapRequest;
import ca.hanson.shiftflow_backend.dto.SwapRequestResponse;
import ca.hanson.shiftflow_backend.service.SwapRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/swap-requests")
public class SwapRequestController {

    private final SwapRequestService swapRequestService;

    @Autowired
    public SwapRequestController(SwapRequestService swapRequestService) {
        this.swapRequestService = swapRequestService;
    }

    @PostMapping
    public ResponseEntity<SwapRequestResponse> createSwapRequest(
            @RequestBody CreateSwapRequest request,
            Authentication authentication
    ) {
        SwapRequestResponse response = swapRequestService.createSwapRequest(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<SwapRequestResponse>> getAll(Authentication authentication) {
        return ResponseEntity.ok(swapRequestService.getAll(authentication));
    }

    @GetMapping("/inbox")
    public ResponseEntity<List<SwapRequestResponse>> getInbox(Authentication authentication) {
        return ResponseEntity.ok(swapRequestService.getInbox(authentication));
    }

    @GetMapping("/sent")
    public ResponseEntity<List<SwapRequestResponse>> getSent(Authentication authentication) {
        return ResponseEntity.ok(swapRequestService.getSent(authentication));
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<SwapRequestResponse> accept(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(swapRequestService.accept(id, authentication));
    }

    @PutMapping("/{id}/decline")
    public ResponseEntity<SwapRequestResponse> decline(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(swapRequestService.decline(id, authentication));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<SwapRequestResponse> approve(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(swapRequestService.approve(id, authentication));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<SwapRequestResponse> reject(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(swapRequestService.reject(id, authentication));
    }
}