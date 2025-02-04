package com.gerasimchuk.droneslogistics.domain.entity;

import com.gerasimchuk.droneslogistics.common.enumeration.CargoState;
import com.gerasimchuk.droneslogistics.common.enumeration.CargoType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "cargo_audit", schema = "public", catalog = "")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class CargoAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cargo_audit_seq_gen")
    @SequenceGenerator(name = "cargo_audit_seq_gen", sequenceName = "cargo_audit_seq", allocationSize = 1)
    private Long id;

    @Column(name = "cargo_id", nullable = false, unique = true)
    private UUID cargoId;

    @Column(name = "drone_id", nullable = false)
    private UUID droneId;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CargoType type;

    @Column(name = "state", nullable = false)
    @Enumerated(EnumType.STRING)
    private CargoState state;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "weight", nullable = false)
    private Float weight;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "image", nullable = false)
    private String image;

    @Column(name = "count", nullable = false)
    private Integer count;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CargoAudit cargo = (CargoAudit) o;
        return id != null && Objects.equals(id, cargo.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
