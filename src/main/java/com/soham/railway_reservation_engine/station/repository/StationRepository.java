package com.soham.railway_reservation_engine.station.repository;

import com.soham.railway_reservation_engine.station.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

import java.util.Optional;

/**
 * Data access for stations; {@code findByCode} resolves the station from its short code.
 */
public interface StationRepository extends JpaRepository<Station, Long > {
    Optional<Station> findByCode(String code);
}
