package com.haryokuncoro.ops.controller;


import com.haryokuncoro.ops.entity.Merchant;
import com.haryokuncoro.ops.service.MerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final MerchantService merchantService;

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "users/login";
    }

    @GetMapping("/merchants")
    public String merchant() {
        return "merchants/list";
    }

}