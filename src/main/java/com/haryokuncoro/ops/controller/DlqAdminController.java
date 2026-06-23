package com.haryokuncoro.ops.controller;


import com.haryokuncoro.ops.dto.ApiResponse;
import com.haryokuncoro.ops.entity.FailedEvent;
import com.haryokuncoro.ops.service.DlqReplayService;
import com.haryokuncoro.ops.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/dlq")
@RequiredArgsConstructor
public class DlqAdminController {

    private final DlqReplayService replayService;

    @PostMapping("/replay/{id}")
    public ResponseEntity<ApiResponse<String>> replay(@PathVariable UUID id) throws Exception {
        replayService.replay(id);
        return ResponseEntity.ok(
                ResponseUtil.success("", "Replay triggered")
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FailedEvent>>> findAll() {
        return ResponseEntity.ok(
                ResponseUtil.success("", replayService.findAll())
        );
    }
}