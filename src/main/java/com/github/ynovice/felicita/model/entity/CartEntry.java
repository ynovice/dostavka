package com.github.ynovice.felicita.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "cart_entries",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"cart_id", "item_id"})
        }
)
@Getter
@Setter
public class CartEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, referencedColumnName = "id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, referencedColumnName = "id")
    private Item item;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "prev_quantity")
    private Integer prevQuantity;

    @Deprecated
    public Long getUserId() {
        return cart != null ? cart.getUserId() : null;
    }

    public Long getItemId() {
        return item != null ? item.getId() : null;
    }

    public void updateQuantity(int difference) {
        quantity += difference;
    }
}
