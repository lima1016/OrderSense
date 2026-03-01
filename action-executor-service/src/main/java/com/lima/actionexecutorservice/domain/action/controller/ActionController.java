package com.lima.actionexecutorservice.domain.action.controller;

import com.lima.actionexecutorservice.domain.action.model.dto.ActionLogResponse;
import com.lima.actionexecutorservice.domain.action.service.ActionExecutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/actions")
@RequiredArgsConstructor
public class ActionController {

    private final ActionExecutorService actionExecutorService;

    @GetMapping
    public ResponseEntity<List<ActionLogResponse>> getAllActionLogs() {
        return ResponseEntity.ok(actionExecutorService.getAllActionLogs());
    }

    @GetMapping("/anomaly-type/{anomalyType}")
    public ResponseEntity<List<ActionLogResponse>> getByAnomalyType(@PathVariable String anomalyType) {
        return ResponseEntity.ok(actionExecutorService.getActionLogsByAnomalyType(anomalyType));
    }

    @GetMapping("/region/{region}")
    public ResponseEntity<List<ActionLogResponse>> getByRegion(@PathVariable String region) {
        return ResponseEntity.ok(actionExecutorService.getActionLogsByRegion(region));
    }
}
