package com.gerasimchuk.droneslogistics.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.time.ZonedDateTime;
import java.util.Objects;

@Entity
@Table(name = "drone_battery_capacities", schema = "public")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class DroneBatteryCapacity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "capacities_seq_gen")
    @SequenceGenerator(name = "capacities_seq_gen", sequenceName = "capacities_seq", allocationSize = 1)
    private Long id;

    @Column(name = "current_capacity", nullable = false)
    private Integer currentCapacity;

    @Column(name = "last_charge_date")
    private ZonedDateTime lastChargeDate;

    @OneToOne
    @JoinColumn(name = "drone_id", referencedColumnName = "id")
    private Drone drone;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        DroneBatteryCapacity capacity = (DroneBatteryCapacity) o;
        return id != null && Objects.equals(id, capacity.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }


}
