package com.haryokuncoro.ops.controller;


import com.haryokuncoro.ops.entity.Merchant;
import com.haryokuncoro.ops.service.MerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @GetMapping
    public Page<Merchant> getAllMerchants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        return merchantService.getAllMerchants(page, size, sortBy, direction);
    }

    @GetMapping("/{id}")
    public Merchant getMerchant(@PathVariable UUID id) {
        return merchantService.getMerchant(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Merchant createMerchant(@RequestBody Merchant merchant) {
        return merchantService.createMerchant(merchant);
    }

    @PutMapping("/{id}")
    public Merchant updateMerchant(
            @PathVariable UUID id,
            @RequestBody Merchant merchant) {

        return merchantService.updateMerchant(id, merchant);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMerchant(@PathVariable UUID id) {
        merchantService.deleteMerchant(id);
    }
}
