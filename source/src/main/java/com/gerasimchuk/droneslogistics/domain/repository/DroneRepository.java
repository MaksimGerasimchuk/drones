package com.gerasimchuk.droneslogistics.domain.repository;

import com.gerasimchuk.droneslogistics.common.enumeration.DroneState;
import com.gerasimchuk.droneslogistics.domain.entity.Drone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DroneRepository extends JpaRepository<Drone, Long> {


    Optional<Drone> findByDroneId(UUID droneId);

    void deleteByDroneId(UUID droneId);

    boolean existsByNumber(String number);

    Page<Drone> findAllByState(DroneState state, Pageable pageable);

}
