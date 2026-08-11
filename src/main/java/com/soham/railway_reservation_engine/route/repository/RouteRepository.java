package com.soham.railway_reservation_engine.route.repository;

import com.soham.railway_reservation_engine.route.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface RouteRepository extends JpaRepository<Route, Long> {
}
