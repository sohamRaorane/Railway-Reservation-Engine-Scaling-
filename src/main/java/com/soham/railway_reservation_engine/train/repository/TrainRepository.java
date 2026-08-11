package com.soham.railway_reservation_engine.train.repository;

import com.soham.railway_reservation_engine.train.entity.Train;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainRepository extends JpaRepository<Train, Long> {

    Optional<Train> findByNumber(String number);

}