package com.sato.alertsgpu.core.repository;

import com.sato.alertsgpu.core.domain.Alert;
import com.sato.alertsgpu.core.domain.AlertHit;
import com.sato.alertsgpu.core.domain.Listing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AlertHitRepository extends JpaRepository<AlertHit, UUID> {
    Optional<AlertHit> findByAlertAndListing(Alert alert, Listing listing);
}