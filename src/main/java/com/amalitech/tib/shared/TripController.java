package com.amalitech.tib.shared;


import com.amalitech.tib.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class TripController {
    @PreAuthorize("hasAuthority('MANAGE_TRIP')")
    @PostMapping
    public ResponseEntity<ApiResponse<String>> createTrip() {

        return ResponseEntity.ok(ApiResponse.success("trip", "Trip created successfully"));
    }
}
