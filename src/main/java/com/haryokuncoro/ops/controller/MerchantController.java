package com.haryokuncoro.ops.controller;


import com.haryokuncoro.ops.dto.ApiResponse;
import com.haryokuncoro.ops.entity.Merchant;
import com.haryokuncoro.ops.service.MerchantService;
import com.haryokuncoro.ops.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @GetMapping
    public ApiResponse getAllMerchants(
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
        Page<Merchant> resp = merchantService.getAllMerchants(name, email, stripeAccountId, pageable);
        return ResponseUtil.success(resp);
    }

    @GetMapping("/{id}")
    public ApiResponse getMerchant(@PathVariable UUID id) {
        return ResponseUtil.success(merchantService.getMerchant(id));
    }

    @PostMapping
    public ApiResponse createMerchant(@RequestBody Merchant merchant) {
        return ResponseUtil.success(merchantService.createMerchant(merchant));
    }

    @PutMapping("/{id}")
    public ApiResponse updateMerchant(
            @PathVariable UUID id,
            @RequestBody Merchant merchant) {

        return ResponseUtil.success(merchantService.updateMerchant(id, merchant));
    }

    @DeleteMapping("/{id}")
    public ApiResponse deleteMerchant(@PathVariable UUID id) {
        merchantService.deleteMerchant(id);
        return ResponseUtil.success("merchant deleted successfully");
    }
}
