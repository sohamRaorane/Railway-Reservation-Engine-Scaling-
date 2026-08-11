package com.soham.railway_reservation_engine.schedule.repository;

import com.soham.railway_reservation_engine.schedule.entity.Schedule;
import com.soham.railway_reservation_engine.train.dto.TrainSearchResponse;
import com.soham.railway_reservation_engine.train.entity.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    //this is for find the schedule for  a train X on journery date Y
    Optional<Schedule> findByTrainAndJourneyDate(Train train, LocalDate journeyDate);
    List<Schedule> findByTrain(Train train);

    //Start with the schedules
    //Get the associated train
    //find one route entry for the soucre station
    //find another route entry for the destination station
    //ensure both routes belong to the same train
    //ensure the source stop comes before the destination stop
    //And build a train serach response directly
    @Query("""
        SELECT new com.soham.railway_reservation_engine.train.dto.TrainSearchResponse(
            t.number, 
            t.name,
            s.journeyDate,
            sourceRoute.departureTime,
            destinationRoute.arrivalTime
        )
        FROM Schedule s
        JOIN s.train t
        JOIN Route sourceRoute ON sourceRoute.train = t
        JOIN Route destinationRoute ON destinationRoute.train = t
        WHERE 
            s.journeyDate = :journeyDate AND
            sourceRoute.station.code = :source AND
            destinationRoute.station.code = :destination AND
            sourceRoute.sequenceNo < destinationRoute.sequenceNo
        
        """)
    List<TrainSearchResponse> searchTrains(


            @Param("source") String source,
            @Param("destination") String destination,
            @Param("journeyDate") LocalDate journeyDate
    );


}