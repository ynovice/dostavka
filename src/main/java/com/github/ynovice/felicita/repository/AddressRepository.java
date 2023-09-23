package com.github.ynovice.felicita.repository;

import com.github.ynovice.felicita.model.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {}
