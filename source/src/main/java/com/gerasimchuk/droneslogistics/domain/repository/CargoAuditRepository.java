package com.gerasimchuk.droneslogistics.domain.repository;

import com.gerasimchuk.droneslogistics.domain.entity.CargoAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CargoAuditRepository extends JpaRepository<CargoAudit, Long> {
}
