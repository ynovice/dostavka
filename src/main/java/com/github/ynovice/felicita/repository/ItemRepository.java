package com.github.ynovice.felicita.repository;

import com.github.ynovice.felicita.model.dto.request.ItemFilterParamsDto;
import com.github.ynovice.felicita.model.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT DISTINCT i FROM Item i " +
            "LEFT JOIN i.categories ctg " +
            "WHERE (i.active = true) " +
            "AND (:#{#filterParams.filterByPriceFrom} = false or i.price >= :#{#filterParams.priceFrom}) " +
            "AND (:#{#filterParams.filterByPriceTo} = false or i.price <= :#{#filterParams.priceTo}) " +
            "AND (:#{#filterParams.filterByCategories} = false or ctg.id IN :#{#filterParams.categoriesIds}) " +
            "ORDER BY i.createdAt")
    Page<Item> findAllByFilterParameters(ItemFilterParamsDto filterParams, Pageable pageable);
}
