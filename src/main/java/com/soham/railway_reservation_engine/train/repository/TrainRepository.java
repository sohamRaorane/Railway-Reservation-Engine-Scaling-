package com.soham.railway_reservation_engine.train.repository;

import com.soham.railway_reservation_engine.train.entity.Train;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Data access for trains; {@code findByNumber} resolves a train from its public number.
 */
public interface TrainRepository extends JpaRepository<Train, Long> {

    Optional<Train> findByNumber(String number);

}