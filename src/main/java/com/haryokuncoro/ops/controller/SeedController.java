package com.haryokuncoro.ops.controller;


import com.haryokuncoro.ops.dto.SeedResponse;
import com.haryokuncoro.ops.service.SeedDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/seed")
@RequiredArgsConstructor
public class SeedController {

    private final SeedDataService seedDataService;

    @PostMapping("/merchants")
    public ResponseEntity<SeedResponse> seedMerchants() {
        return ResponseEntity.ok(seedDataService.seed());
    }
}