package com.sato.alertsgpu.repository;

import com.sato.alertsgpu.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, UUID> {
    List<Alert> findByEnabledTrue();
}
