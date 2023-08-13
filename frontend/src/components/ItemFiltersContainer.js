import {useSearchParams} from "react-router-dom";
import Api from "../Api";
import React, {useEffect, useState} from "react";
import "../css/ItemsFilter.css";
import ItemCategoryFilter from "./ItemCategoryFilter";
import ItemCheckboxFilter from "./ItemCheckboxFilter";

function ItemFiltersContainer({opened, setOpened, setItemsPage, setCatalogState, onSuccessCatalogState}) {

    const [searchParams] = useSearchParams();

    useEffect(() => {

        const abortController = new AbortController();

        Api.getItemsPageByFilterParams(searchParams, abortController.signal)
            .then(itemsPage => {
                setItemsPage(itemsPage);
                setCatalogState(onSuccessCatalogState);
            });

        return () => abortController.abort();
    }, [onSuccessCatalogState, searchParams, setCatalogState, setItemsPage]);

    const [priceFrom, setPriceFrom] = useState(
        searchParams.get("priceFrom") !== null ? searchParams.get("priceFrom") :
            ""
    );

    const [priceTo, setPriceTo] = useState(
        searchParams.get("priceTo") !== null ? searchParams.get("priceTo") :
            ""
    );

    const [existingCategoriesTrees, setExistingCategoriesTrees] = useState([]);
    const [chosenCategoriesIds, setChosenCategoriesIds] = useState([]);
    useEffect(() => {
        Api.getAllCategories()
            .then(retrievedCategoriesTrees => setExistingCategoriesTrees(retrievedCategoriesTrees));
        }, []);


    const [existingColors, setExistingColors] = useState([]);
    const [chosenColorsIds, setChosenColorsIds] = useState([]);
    useEffect(() => {
        Api.getAllColors().then(retrievedColors => setExistingColors(retrievedColors));
        }, []);


    const handleApplyFiltersClick = () => {

        const searchParamsDto = buildNewSearchParams();

        window.location.href = window.location.href.split("?")[0] +
            "?" +
            new URLSearchParams(searchParamsDto);
    }

    const buildNewSearchParams = () => {

        const searchParamsDto = {};

        if(searchParams.get("page") !== null) searchParamsDto.page = searchParams.get("page");

        if(priceFrom !== "") searchParamsDto.priceFrom = priceFrom;
        if(priceTo !== "") searchParamsDto.priceTo = priceTo;

        if(chosenCategoriesIds.length > 0) searchParamsDto.categoriesIds = chosenCategoriesIds;
        if(chosenColorsIds.length > 0) searchParamsDto.colorsIds = arrToStr(chosenColorsIds);

        return searchParamsDto;
    }

    const arrToStr = (arr) => {

        let result = "";

        for (let i = 0; i < arr.length; i++) {
            result += String(arr[i]) + ",";
        }

        if(result.length > 0) result = result.substring(0, result.length - 1);

        return result;
    }

    return (
        <React.Fragment>
            <div className={"filters-background-darkener" + (opened ? " opened" : "")}></div>

            <div className={"ItemsFilter" + (opened ? " opened" : "")}>

                <div className="heading">
                    <p className="title">Фильтры</p>
                    <span className="link danger" onClick={() => setOpened(!opened)}>Скрыть</span>
                </div>

                <div className="inputs-filter">
                    <div className="filter-name">Цена</div>
                    <div>
                        <label htmlFor="priceFrom">От (₽)</label>
                        <input className="flct-input"
                               id="priceFrom"
                               type="number"
                               value={priceFrom}
                               onChange={(e) => setPriceFrom(e.target.value)}/>
                    </div>
                    <div>
                        <label htmlFor="priceTo">До (₽)</label>
                        <input className="flct-input"
                               id="priceTo"
                               type="number"
                               value={priceTo}
                               onChange={(e) => setPriceTo(e.target.value)}/>
                    </div>
                </div>

                <ItemCategoryFilter getParamName={"categoriesIds"}
                                    title={"Категории"}
                                    categoriesTrees={existingCategoriesTrees}
                                    chosenIds={chosenCategoriesIds}
                                    setChosenIds={setChosenCategoriesIds}/>

                <ItemCheckboxFilter getParamName={"colorsIds"}
                                    title={"Цвет"}
                                    options={existingColors}
                                    setIds={setChosenColorsIds}/>

                <input type="button"
                       className="button"
                       value="Применить"
                       onClick={() => handleApplyFiltersClick()}/>

                <span onClick={() => window.location.href = window.location.href.split("?")[0]}
                      className="link danger">Сбросить</span>
            </div>
        </React.Fragment>
    );
}

export default ItemFiltersContainer;