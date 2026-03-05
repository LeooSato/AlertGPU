package com.sato.alertsgpu.controller;

import com.sato.alertsgpu.model.Alert;
import com.sato.alertsgpu.model.dto.AlertResponse;
import com.sato.alertsgpu.model.dto.CreateAlertRequest;
import com.sato.alertsgpu.model.dto.UpdateAlertRequest;
import com.sato.alertsgpu.repository.AlertRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertRepository alertRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlertResponse create(@RequestBody @Valid CreateAlertRequest req) {
        validateRange(req.priceMin(), req.priceMax());

        Alert alert = Alert.builder()
                .name(req.name())
                .gpuFamily(req.gpuFamily() == null || req.gpuFamily().isBlank() ? "RTX" : req.gpuFamily().trim().toUpperCase())
                .gpuModel(req.gpuModel())
                .vramGb(req.vramGb())
                .priceMin(req.priceMin())
                .priceMax(req.priceMax())
                .stores(req.stores())
                .enabled(true)
                .build();

        Alert saved = alertRepository.save(alert);
        return toResponse(saved);
    }

    @GetMapping
    public List<AlertResponse> list() {
        return alertRepository.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public AlertResponse get(@PathVariable UUID id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + id));
        return toResponse(alert);
    }

    @PutMapping("/{id}")
    public AlertResponse update(@PathVariable UUID id, @RequestBody @Valid UpdateAlertRequest req) {
        validateRange(req.priceMin(), req.priceMax());

        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + id));

        alert.setName(req.name());
        alert.setGpuFamily(req.gpuFamily() == null || req.gpuFamily().isBlank() ? "RTX" : req.gpuFamily().trim().toUpperCase());
        alert.setGpuModel(req.gpuModel());
        alert.setVramGb(req.vramGb());
        alert.setPriceMin(req.priceMin());
        alert.setPriceMax(req.priceMax());
        alert.setStores(req.stores());
        alert.setEnabled(req.enabled());

        Alert saved = alertRepository.save(alert);
        return toResponse(saved);
    }

    @PatchMapping("/{id}/toggle")
    public AlertResponse toggle(@PathVariable UUID id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + id));

        alert.setEnabled(!Boolean.TRUE.equals(alert.getEnabled()));
        return toResponse(alertRepository.save(alert));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        if (!alertRepository.existsById(id)) {
            throw new IllegalArgumentException("Alert not found: " + id);
        }
        alertRepository.deleteById(id);
    }

    private void validateRange(java.math.BigDecimal min, java.math.BigDecimal max) {
        if (min.compareTo(max) > 0) {
            throw new IllegalArgumentException("priceMin must be <= priceMax");
        }
    }

    private AlertResponse toResponse(Alert a) {
        return new AlertResponse(
                a.getId(),
                a.getName(),
                a.getGpuFamily(),
                a.getGpuModel(),
                a.getVramGb(),
                a.getPriceMin(),
                a.getPriceMax(),
                a.getEnabled(),
                a.getStores(),
                a.getCreatedAt()
        );
    }
}