package com.gerasimchuk.droneslogistics.domain.repository;

import com.gerasimchuk.droneslogistics.domain.entity.Cargo;
import com.gerasimchuk.droneslogistics.domain.entity.Drone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CargoRepository extends JpaRepository<Cargo, Long> {
    Optional<Cargo> findByDroneAndNameAndCode(Drone drone, String name, String code);
}
