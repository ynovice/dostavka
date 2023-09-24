package com.github.ynovice.felicita.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "addresses")
@Getter
@Setter
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lon;

    @Column(nullable = false, length = 1000)
    private String representation;

    @Column(nullable = false)
    private ZonedDateTime createdAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(referencedColumnName = "id")
    private User user;

    @OneToMany(mappedBy = "address")
    private List<Order> orders;

    public boolean hasOrders() {
        return orders != null && !orders.isEmpty();
    }
}
