import withHeaderAndFooter from "../hoc/withHeaderAndFooter";
import requiresUser from "../hoc/requiresUser";
import "../css/SaveItemPage.css";
import React, {useContext, useEffect, useRef, useState} from "react";
import Api from "../Api";
import InvalidEntityException from "../exception/InvalidEntityException";
import adminAccessOnly from "../hoc/adminAccessOnly";
import ItemPropertySelector from "../components/ItemPropertySelector";
import ItemCategoriesSelector from "../components/ItemCategoriesSelector";
import {useSearchParams} from "react-router-dom";
import {ApiContext} from "../contexts/ApiContext";
import imageApi from "../api/ImageApi";

function SaveItemPage() {

    const [categoriesTrees, setCategoriesTrees] = useState([]);

    /*
        [
            [rootCategoryId1, subCategoryId, subSubCategoryId, ...],
            ...
        ]
     */
    const [selectedCategoriesSequences, setSelectedCategoriesSequences] = useState([]);
    useEffect(() => {
        Api.getAllCategories()
            .then(categories => setCategoriesTrees(categories))
            .catch(() => alert("Ошибка при получении списка категорий, свяжитесь с разработчиком"));
    }, []);


    const [colors, setColors] = useState([]);
    const [selectedColorsIds, setSelectedColorsIds] = useState([]);
    useEffect(() => {
        Api.getAllColors()
            .then(retrievedColors => setColors(retrievedColors))
            .catch(() => alert("Ошибка при получении списка цветов, свяжитесь с разработчиком"));
    }, []);


    const [imagesData, setImagesData] = useState([]);
    const [ongoingUploadImagesNames, setOngoingUploadImagesNames] = useState([]);
    const [imageUploaderRef] = useState(useRef());

    const handleAddImageInput = (e) => {
        e.preventDefault();
        imageUploaderRef.current.click();
    }

    const handleImageUpload = (e) => {

        e.preventDefault();

        const image = e.target.files[0];

        if(!image) {
            return;
        }

        addOngoingUploadImageName(image["name"]);

        Api.uploadImage(image, new AbortController().signal)
            .then(uploadedImage => {
                removeOngoingUploadImageName(image["name"]);
                const updatedImagesData = structuredClone(imagesData);
                updatedImagesData.push({
                    uploadedImage,
                    name: image["name"]
                });
                setImagesData(updatedImagesData);
            })
            .catch(() => {
                removeOngoingUploadImageName(image["name"]);
                alert("Ошибка при попытке загрузить изображение, свяжитесь с разработчиком");
            });
    }

    const addOngoingUploadImageName = (name) => {
        const updatedOngoingUploadImagesNames = structuredClone(ongoingUploadImagesNames);
        updatedOngoingUploadImagesNames.push(name);
        setOngoingUploadImagesNames(updatedOngoingUploadImagesNames);
    };

    const removeOngoingUploadImageName = (name) => {
        const updatedOngoingUploadImagesNames = structuredClone(ongoingUploadImagesNames);
        updatedOngoingUploadImagesNames.splice(updatedOngoingUploadImagesNames.indexOf(name), 1);
        setOngoingUploadImagesNames(updatedOngoingUploadImagesNames);
    }

    const removeImageByIndex = (i) => {

        const imageId = imagesData[i]["uploadedImage"]["id"];

        imageApi.deleteById(imageId)
            .then(() => {
                const updatedImagesData = structuredClone(imagesData);
                updatedImagesData.splice(i, 1);
                setImagesData(updatedImagesData);
            });
    }


    const [name, setName] = useState("");
    const [description, setDescription] = useState("");
    const [price, setPrice] = useState("");
    const [quantity, setQuantity] = useState("");


    const [fieldErrors, setFieldErrors] = useState([]);

    const handleSaveItem = (e) => {
        e.preventDefault();

        const imagesIds = [];
        for (let i = 0; i < imagesData.length; i++) imagesIds.push(imagesData[i]["uploadedImage"]["id"]);

        const categoriesIds = [];
        for (let i = 0; i < selectedCategoriesSequences.length; i++)
            categoriesIds.push(...selectedCategoriesSequences[i]);

        const modifyItemRequestDto = {
            name, description, price, quantity, imagesIds, categoriesIds,
            colorsIds: selectedColorsIds
        };

        const itemId = Number(searchParams.get("id"));

        if(!itemId) {
            itemApi.create(modifyItemRequestDto)
                .then(responseBody => {
                    if(responseBody["id"]) {
                        window.location.href = "/item/" + responseBody["id"];
                    }
                }).catch(e => {
                    if(e instanceof InvalidEntityException) {
                        setFieldErrors(e.validationResult.fieldErrors);
                    }
                console.log(e);

                });
        } else {
            itemApi.update(itemId, modifyItemRequestDto)
                .then(responseBody => {
                    if(responseBody["id"]) {
                        window.location.href = "/item/" + responseBody["id"];
                    }
                }).catch(e => {
                console.log(e);
                    if(e instanceof InvalidEntityException) {
                        setFieldErrors(e.validationResult.fieldErrors);
                    }
                });
        }
    }

    const [searchParams] = useSearchParams();
    const { itemApi } = useContext(ApiContext);

    useEffect(() => {

        const id = Number(searchParams.get("id"));

        if(!id) return;

        const abortController = new AbortController();
        itemApi.getById(id, abortController.signal)
            .then(retrievedItem => {

                setName(retrievedItem["name"]);
                setDescription(retrievedItem["description"]);
                setPrice(retrievedItem["price"]);
                setQuantity(retrievedItem["quantity"])

                setSelectedColorsIds(retrievedItem["colors"].map(color => color["id"]));

                setImagesData(retrievedItem["images"].map(image => {
                    return {uploadedImage: image, name: `id ${image["id"]} image`}
                }));

                setSelectedCategoriesSequences(getCategorySequences(
                    categoriesTrees,
                    retrievedItem["categories"].map(category => category["id"])
                ));

            });

        return () => abortController.abort();
    }, [categoriesTrees, itemApi, searchParams]);

    function getCategorySequences(categoriesTrees, plainCategoriesIdsList) {

        const sequences = [];

        function findCategory(category, currentSequence) {

            if(plainCategoriesIdsList.includes(category["id"]))
                currentSequence.push(category["id"]);

            if (category["subCategories"].length > 0) {
                for (const subCategory of category["subCategories"]) {
                    findCategory(subCategory, [...currentSequence]);
                }
            } else {
                sequences.push(currentSequence);
            }
        }

        for (const categoryTree of categoriesTrees) {
            if (plainCategoriesIdsList.includes(categoryTree.id)) {
                findCategory(categoryTree, []);
            }
        }

        return sequences;
    }

    return (
        <div className="SaveItemPage">
            <div className="section no-margin">
                <h1 className="page-title">Создание товара</h1>
            </div>

            <div className="section block-title">Основное</div>
            <div className="section form-row no-margin">
                <label htmlFor="name">Название</label>
                <input className="flct-input"
                       value={name}
                       onChange={(e) => setName(e.target.value)}
                       type="text"
                       id="name"
                       placeholder="Название товара"/>
            </div>
            <div className="section form-row no-margin">
                <label htmlFor="description">Описание</label>
                <input className="flct-input"
                       value={description}
                       onChange={(e) => setDescription(e.target.value)}
                       type="text"
                       id="description"
                       placeholder="Описание товара"/>
            </div>
            <div className="section form-row no-margin">
                <label htmlFor="price">Цена (₽)</label>
                <input className="flct-input"
                       value={price}
                       onChange={(e) => setPrice(e.target.value)}
                       type="number"
                       id="price"
                       placeholder="Цена товара"/>
            </div>

            <div className="section form-row">
                <label htmlFor="price">Количество</label>
                <input className="flct-input"
                       value={quantity}
                       onChange={(e) => setQuantity(e.target.value)}
                       type="number"
                       id="price"
                       placeholder="Количество товара"/>
            </div>

            <div className="section block-title">Фотографии</div>
            <div className="section form-row col">

                <input type="file"
                       id="file-uploader"
                       onChange={(e) => handleImageUpload(e)}
                       ref={imageUploaderRef}/>

                {imagesData.length === 0 && <p>Фотографии не добавлены</p>}
                {imagesData.length !== 0 && imagesData.map((imageData, i) => {
                    return (
                        <div key={imageData["uploadedImage"]["id"]} className="selects-row">
                            <img src={imageApi.getImageUrlByImageId(imageData["uploadedImage"]["id"])}
                                 alt={imageData["uploadedImage"]["id"]}/>
                            <span>{imageData["name"]}</span><span className="status-ready">загружена</span>
                            <span onClick={() => removeImageByIndex(i)}
                                  className="link danger">
                                Удалить
                            </span>
                        </div>
                    );
                })}
                {ongoingUploadImagesNames.length !== 0 && ongoingUploadImagesNames.map((name, index)=> {
                    return (
                        <div className="selects-row" key={name + index}>
                            <span>{name}</span><span className="status-uploading">загружается</span>
                        </div>
                    );
                })}

                <span className="link"
                      onClick={(e) => handleAddImageInput(e)}>
                    Добавить
                </span>
            </div>

            <div className="section block-title">Категории</div>
            <ItemCategoriesSelector emptyListMessage="Категории не указаны"
                                    existingCategoriesTrees={categoriesTrees}
                                    selectedCategoriesSequences={selectedCategoriesSequences}
                                    setSelectedCategoriesSequences={setSelectedCategoriesSequences}/>

            <div className="section block-title">Цвета</div>
            <ItemPropertySelector emptyListMessage="Цвета не указаны"
                                  selectedIds={selectedColorsIds}
                                  setSelectedIds={setSelectedColorsIds}
                                  propertySource={colors}/>
            
            <div className="section">
                <input type="button"
                       className="button"
                       onClick={(e) => handleSaveItem(e)}
                       value="Сохранить"/>
            </div>

            <div className="errors-container">
                {fieldErrors.map(fieldError => {
                    return <p key={fieldError.errorCode}>{fieldError.errorMessage}</p>
                })}
            </div>
        </div>
    );
}

export default withHeaderAndFooter(adminAccessOnly(requiresUser(
    SaveItemPage,
    "Чтобы просмотреть эту страницу, нужно войти в аккаунт администратора."
)));