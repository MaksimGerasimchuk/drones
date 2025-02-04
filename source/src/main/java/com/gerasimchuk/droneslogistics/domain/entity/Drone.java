package com.gerasimchuk.droneslogistics.domain.entity;

import com.gerasimchuk.droneslogistics.common.enumeration.DroneModelType;
import com.gerasimchuk.droneslogistics.common.enumeration.DroneState;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "drones", schema = "public", catalog = "")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Drone {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "drones_seq_gen")
    @SequenceGenerator(name = "drones_seq_gen", sequenceName = "drones_seq", allocationSize = 1)
    private Long id;

    @Column(name = "drone_id", nullable = false, unique = true)
    private UUID droneId;

    @Version
    @Column(name = "version")
    private Integer version;

    @Column(name = "number", nullable = false, unique = true)
    private String number;

    @Column(name = "model")
    @Enumerated(EnumType.STRING)
    private DroneModelType model;

    @Column(name = "weight_limit")
    private Integer weightLimit;

    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private DroneState state;

    @Column(name = "last_state_update")
    private ZonedDateTime lastStateUpdate;

    @OneToOne(mappedBy = "drone", cascade = CascadeType.ALL, orphanRemoval = true)
    private DroneBatteryCapacity batteryCapacity;

    @OneToMany(mappedBy = "drone", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Cargo> cargoList = new ArrayList<>();

    public void addCargo(Cargo cargo) {
        this.cargoList.add(cargo);
        cargo.setDrone(this);
    }

    public void removeCargo(Cargo cargo) {
        this.cargoList.remove(cargo);
        cargo.setDrone(null);
    }

    public void removeAllCargo() {
        this.cargoList.forEach(c -> c.setDrone(null));
        this.cargoList.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Drone drone = (Drone) o;
        return id != null && Objects.equals(id, drone.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
