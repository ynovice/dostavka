package com.github.ynovice.felicita.repository;

import com.github.ynovice.felicita.model.entity.OrderEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderEntryRepository extends JpaRepository<OrderEntry, Long> {}
