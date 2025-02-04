package com.gerasimchuk.droneslogistics.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "drone_battery_capacity_audit", schema = "public")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class DroneBatteryCapacityAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "battery_audit_seq_gen")
    @SequenceGenerator(name = "battery_audit_seq_gen", sequenceName = "battery_audit_seq", allocationSize = 1)
    private Long id;

    @Column(name = "drone_id", nullable = false)
    private UUID droneId;

    @Column(name = "check_date_time")
    private ZonedDateTime checkDateTime;

    @Column(name = "battery_capacity")
    private Integer batteryCapacity;
}
