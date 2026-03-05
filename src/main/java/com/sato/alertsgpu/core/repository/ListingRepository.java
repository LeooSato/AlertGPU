package com.sato.alertsgpu.core.repository;

import com.sato.alertsgpu.core.domain.Listing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ListingRepository extends JpaRepository<Listing, UUID> {
    Optional<Listing> findByFingerprint(String fingerprint);

}