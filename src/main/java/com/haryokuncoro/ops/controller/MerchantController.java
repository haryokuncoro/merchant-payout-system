package com.haryokuncoro.ops.controller;


import com.haryokuncoro.ops.entity.Merchant;
import com.haryokuncoro.ops.service.MerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @GetMapping
    public Page<Merchant> getAllMerchants(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String stripeAccountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return merchantService.getAllMerchants(name, email, stripeAccountId, pageable);
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
