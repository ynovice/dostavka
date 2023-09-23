package com.github.ynovice.felicita.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "order_entries")
@Getter
@Setter
public class OrderEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(referencedColumnName = "id", nullable = false)
    private Item item;

    @Column(nullable = false)
    private Integer pricePerItem;

    @Column(nullable = false)
    private Integer quantity;

    @ManyToOne
    @JoinColumn(referencedColumnName = "id", nullable = false)
    private Order order;
}
