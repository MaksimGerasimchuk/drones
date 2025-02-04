package com.gerasimchuk.droneslogistics.domain.repository;

import com.gerasimchuk.droneslogistics.domain.entity.DroneBatteryCapacity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DroneBatteryCapacityRepository extends JpaRepository<DroneBatteryCapacity, Long> {

    Page<DroneBatteryCapacity> findAllByCurrentCapacityEquals(Integer capacity, Pageable pageable);

    List<DroneBatteryCapacity> findAllByCurrentCapacityGreaterThan(Integer capacity);
}
