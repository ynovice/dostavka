package com.github.ynovice.felicita.validator;

import com.github.ynovice.felicita.model.entity.Category;
import com.github.ynovice.felicita.model.entity.Image;
import com.github.ynovice.felicita.model.entity.Item;
import com.github.ynovice.felicita.repository.CategoryRepository;
import com.github.ynovice.felicita.repository.ImageRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ItemValidator implements Validator {

    private final ImageRepository imageRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public boolean supports(@NonNull Class<?> targetObjectClass) {
        return targetObjectClass.equals(Item.class);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {

        Item item = (Item) target;

        validateName(item, errors);
        validateDescription(item, errors);
        validateImages(item, errors);
        validateCategories(item, errors);
        validateCreatedAt(item, errors);
        validatePrice(item, errors);
        validateQuantity(item, errors);
        validateActive(item, errors);
    }

    public void validateName(@NonNull Item item, @NonNull Errors errors) {

        if(!StringUtils.hasText(item.getName())) {
            errors.rejectValue(
                    "name",
                    "item.name.empty",
                    "Имя товара не может быть пустым"
            );
            return;
        }

        if(item.getName().length() > 100) {
            errors.rejectValue(
                    "name",
                    "item.name.tooLong",
                    "Имя товара должно быть не длиньше 100 символов"
            );
        }
    }

    public void validateDescription(@NonNull Item item, @NonNull Errors errors) {

        String description = item.getDescription();

        if(description == null) {
            errors.rejectValue(
                    "description",
                    "item.description.null",
                    "Описание товара имеет недопустимое значение, свяжитесь с разработчиком"
            );
            return;
        }

        if(!description.isEmpty() && description.trim().isEmpty()) {
            errors.rejectValue(
                    "description",
                    "item.description.empty",
                    "Описание товара не может состоять только из пробелов"
            );
            return;
        }

        if(description.length() > 1000) {
            errors.rejectValue(
                    "description",
                    "item.description.tooLong",
                    "Описание товара должно быть не длиньше 1000 символов"
            );
        }
    }

    public void validateImages(@NonNull Item item, @NonNull Errors errors) {

        List<Image> images = item.getImages();

        if(images == null) {
            errors.rejectValue(
                    "images",
                    "item.images.null",
                    "Произошла ошибка при привязке изображений к товару, свяжитесь с разработчиком"
            );
            return;
        }

        images
                .stream()
                .filter(image -> !imageRepository.existsById(image.getId()))
                .findAny()
                .ifPresent(value -> errors.rejectValue(
                        "images",
                        "item.images.dontExist",
                        "Произошла ошибка при загрузке одного или нескольких изображений"
                ));

        for (int i = 0; i < images.size() - 1; i++) {
            for (int j = i + 1; j < images.size(); j++) {
                if (images.get(i).getId().equals(images.get(j).getId())) {
                    errors.rejectValue(
                            "images",
                            "item.images.duplicated",
                            "Одна и та же фотография указана несколько раз"
                    );
                    return;
                }
            }
        }
    }

    public void validateCategories(@NonNull Item item, @NonNull Errors errors) {

        List<Category> categories = item.getCategories();

        if(categories == null) {
            errors.rejectValue(
                    "categories",
                    "item.categories.null",
                    "Список категорий товара имеет недопустимое значение, свяжитесь с разработчиком"
            );
            return;
        }

        categories
                .stream()
                .filter(category -> !categoryRepository.existsById(category.getId()))
                .findAny()
                .ifPresent(value -> errors.rejectValue(
                        "categories",
                        "item.categories.dontExist",
                        "Среди выбранных категорий есть одна или несколько несуществующих категорий"
                ));

        for (int i = 0; i < categories.size() - 1; i++) {
            for (int j = i + 1; j < categories.size(); j++) {
                if (categories.get(i).getId().equals(categories.get(j).getId())) {
                    errors.rejectValue(
                            "categories",
                            "item.categories.duplicated",
                            "Одна и та же категория указана несколько раз"
                    );
                    return;
                }
            }
        }

        for(Category category : categories) {

            Optional<Long> parentId = categoryRepository.findParentIdById(category.getId());

            while(parentId.isPresent()) {

                Optional<Long> finalParentId = parentId;
                boolean bothParentAndSubcategoryArePresent = categories
                        .stream()
                        .anyMatch(c -> finalParentId.get().equals(c.getId()));

                if(!bothParentAndSubcategoryArePresent) {
                    errors.rejectValue(
                            "categories",
                            "item.categories.duplicated",
                            "Нельзя указать дочернюю категорию, не указав при этом родительскую категорию"
                    );
                    return;
                }

                parentId = categoryRepository.findParentIdById(parentId.get());
            }
        }
    }

    public void validateCreatedAt(@NonNull Item item, @NonNull Errors errors) {

        if(item.getCreatedAt() == null)
            errors.rejectValue(
                    "createdAt",
                    "item.createdAt.null",
                    "Дата создания товара имеет недопустимое значение, свяжитесь с разработчиком"
            );
    }

    public void validatePrice(@NonNull Item item, @NonNull Errors errors) {

        if(item.getPrice() == null) {
            errors.rejectValue(
                    "price",
                    "item.price.null",
                    "Укажите стоимость товара"
            );
            return;
        }

        if(item.getPrice() <= 0) {
            errors.rejectValue(
                    "price",
                    "item.price.notPositive",
                    "Цена товара должна быть больше 0"
            );
        }
    }

    public void validateQuantity(@NonNull Item item, @NonNull Errors errors) {

        if(item.getQuantity() == null) {
            errors.rejectValue(
                    "quantity",
                    "item.quantity.null",
                    "Укажите количество товара"
            );
            return;
        }

        if(item.getQuantity() < 0) {
            errors.rejectValue(
                    "quantity",
                    "item.quantity.notPositive",
                    "Количество товара не может быть меньше 0"
            );
        }
    }


    public void validateActive(@NonNull Item item, @NonNull Errors errors) {

        if(item.getActive() == null) {
            errors.rejectValue(
                    "active",
                    "item.active.null",
                    "Поле active имеет недопустимое значение, свяжитесь с разработчиком"
            );
            return;
        }

        if(!item.getActive().equals(shouldBeActive(item))) {
            errors.rejectValue(
                    "active",
                    "item.active.invalid",
                    "Произошла ошибка при вычислении значения active, свяжитесь с разработчиком"
            );
        }
    }

    private boolean shouldBeActive(@NonNull Item item) {
        return item.getQuantity() > 0;
    }
}
