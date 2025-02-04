package com.gerasimchuk.droneslogistics.domain.repository;

import com.gerasimchuk.droneslogistics.domain.entity.DroneBatteryCapacityAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatteryAuditRepository extends JpaRepository<DroneBatteryCapacityAudit, Long> {
}
