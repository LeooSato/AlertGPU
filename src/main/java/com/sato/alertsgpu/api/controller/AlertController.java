package com.sato.alertsgpu.api.controller;

import com.sato.alertsgpu.core.service.AlertService;
import com.sato.alertsgpu.api.dto.AlertResponse;
import com.sato.alertsgpu.api.dto.CreateAlertRequest;
import com.sato.alertsgpu.api.dto.UpdateAlertRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Slf4j
public class AlertController {

    private final AlertService alertService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new alert")
    public AlertResponse create(@RequestBody @Valid CreateAlertRequest req) {
        return alertService.create(req);
    }

    @GetMapping
    @Operation(summary = "List all alerts")
    public List<AlertResponse> list() {
        return alertService.listAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get alert by ID")
    public AlertResponse get(@PathVariable UUID id) {
        return alertService.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update alert")
    public AlertResponse update(@PathVariable UUID id, @RequestBody @Valid UpdateAlertRequest req) {
        return alertService.update(id, req);
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Toggle alert enabled status")
    public AlertResponse toggle(@PathVariable UUID id) {
        return alertService.toggle(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete alert")
    public void delete(@PathVariable UUID id) {
        alertService.delete(id);
    }
}