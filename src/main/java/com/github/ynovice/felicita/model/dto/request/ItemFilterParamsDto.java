package com.github.ynovice.felicita.model.dto.request;

import lombok.Getter;

import java.util.List;

@Getter
public class ItemFilterParamsDto {

    private Integer priceFrom;
    private boolean filterByPriceFrom = false;

    private Integer priceTo;
    private boolean filterByPriceTo = false;

    private List<Long> materialsIds;
    private boolean filterByMaterials = false;

    private List<Long> categoriesIds;
    private boolean filterByCategories = false;

    private List<Long> colorsIds;
    private boolean filterByColors = false;


    public void setPriceFrom(Integer priceFrom) {
        this.priceFrom = priceFrom;
        filterByPriceFrom = priceFrom != null;
    }

    public void setPriceTo(Integer priceTo) {
        this.priceTo = priceTo;
        filterByPriceTo = priceTo != null;
    }

    public void setMaterialsIds(List<Long> materialsIds) {
        this.materialsIds = materialsIds;
        filterByMaterials = materialsIds != null && !materialsIds.isEmpty();
    }

    public void setCategoriesIds(List<Long> categoriesIds) {
        this.categoriesIds = categoriesIds;
        filterByCategories = categoriesIds != null && !categoriesIds.isEmpty();
    }

    public void setColorsIds(List<Long> colorsIds) {
        this.colorsIds = colorsIds;
        filterByColors = colorsIds != null && !colorsIds.isEmpty();
    }
}
