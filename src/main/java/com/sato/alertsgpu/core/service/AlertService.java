package com.sato.alertsgpu.core.service;

import com.sato.alertsgpu.core.domain.Alert;
import com.sato.alertsgpu.api.dto.AlertResponse;
import com.sato.alertsgpu.api.dto.CreateAlertRequest;
import com.sato.alertsgpu.api.dto.UpdateAlertRequest;
import com.sato.alertsgpu.core.repository.AlertRepository;
import com.sato.alertsgpu.api.exception.AlertNotFoundException;
import com.sato.alertsgpu.api.exception.InvalidPriceRangeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AlertService {

    private final AlertRepository alertRepository;

    public AlertResponse create(CreateAlertRequest req) {
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

    @Transactional(readOnly = true)
    public List<AlertResponse> listAll() {
        return alertRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AlertResponse getById(UUID id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new AlertNotFoundException("Alert not found: " + id));
        return toResponse(alert);
    }

    public AlertResponse update(UUID id, UpdateAlertRequest req) {
        validateRange(req.priceMin(), req.priceMax());

        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new AlertNotFoundException("Alert not found: " + id));

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

    public AlertResponse toggle(UUID id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new AlertNotFoundException("Alert not found: " + id));

        alert.setEnabled(!Boolean.TRUE.equals(alert.getEnabled()));
        return toResponse(alertRepository.save(alert));
    }

    public void delete(UUID id) {
        if (!alertRepository.existsById(id)) {
            throw new AlertNotFoundException("Alert not found: " + id);
        }
        alertRepository.deleteById(id);
    }

    private void validateRange(BigDecimal min, BigDecimal max) {
        if (min.compareTo(max) > 0) {
            throw new InvalidPriceRangeException("priceMin must be <= priceMax");
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
