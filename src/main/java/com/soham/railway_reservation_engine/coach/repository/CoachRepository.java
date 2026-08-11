package com.soham.railway_reservation_engine.coach.repository;

import com.soham.railway_reservation_engine.coach.entity.Coach;
import com.soham.railway_reservation_engine.common.enums.CoachType;
import com.soham.railway_reservation_engine.train.entity.Train;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoachRepository extends JpaRepository<Coach, Long> {
    //fetch all the coaches attached to this train
    List<Coach> findByTrain(Train train);
    List<Coach> findByTrainAndCoachType(Train train, CoachType coachType);
}
