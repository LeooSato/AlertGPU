package com.sato.alertsgpu.repository;

import com.sato.alertsgpu.model.Alert;
import com.sato.alertsgpu.model.AlertHit;
import com.sato.alertsgpu.model.Listing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AlertHitRepository extends JpaRepository<AlertHit, UUID> {
    Optional<AlertHit> findByAlertAndListing(Alert alert, Listing listing);
}