package com.soham.railway_reservation_engine.quota.repository;

import com.soham.railway_reservation_engine.quota.entity.Quota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;

public interface QuotaRepository extends JpaRepository<Quota, Long> {
    Optional<Quota> findByCode(String code);
}
