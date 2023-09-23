package com.github.ynovice.felicita.repository;

import com.github.ynovice.felicita.model.entity.Order;
import com.github.ynovice.felicita.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findAllByUser(User user, Pageable pageable);
}
