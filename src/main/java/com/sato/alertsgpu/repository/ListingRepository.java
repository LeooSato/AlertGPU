package com.sato.alertsgpu.repository;

import com.sato.alertsgpu.model.Listing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ListingRepository extends JpaRepository<Listing, UUID> {
    Optional<Listing> findByFingerprint(String fingerprint);

}