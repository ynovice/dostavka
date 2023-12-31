package com.github.ynovice.felicita.service.impl;

import com.github.ynovice.felicita.exception.InvalidEntityException;
import com.github.ynovice.felicita.exception.NotFoundException;
import com.github.ynovice.felicita.model.dto.request.ItemFilterParamsDto;
import com.github.ynovice.felicita.model.dto.request.ModifyItemRequestDto;
import com.github.ynovice.felicita.model.entity.Image;
import com.github.ynovice.felicita.model.entity.Item;
import com.github.ynovice.felicita.model.entity.Order;
import com.github.ynovice.felicita.model.entity.OrderEntry;
import com.github.ynovice.felicita.repository.*;
import com.github.ynovice.felicita.service.ImageService;
import com.github.ynovice.felicita.service.ItemService;
import com.github.ynovice.felicita.validator.ItemValidator;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;

import java.time.ZonedDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final static int ITEMS_PER_PAGE = 20;

    private final ItemValidator itemValidator;

    private final ImageService imageService;

    private final ItemRepository itemRepository;

    private final ImageRepository imageRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;
    private final OrderEntryRepository orderEntryRepository;

    @Override
    public Item createItem(@NonNull ModifyItemRequestDto requestDto) {

        Item item = new Item();

        injectDtoDataToItem(requestDto, item);

        item.setCartEntries(new HashSet<>());
        item.setCreatedAt(ZonedDateTime.now());
        item.setActive(shouldBeActive(item));

        BindingResult validationResult = validate(item);
        if(validationResult.hasErrors()) {
            throw new InvalidEntityException(validationResult);
        }

        itemRepository.save(item);
        imageRepository.saveAll(item.getImages());

        return item;
    }

    @Override
    @Transactional
    public Item updateItem(Long id, @NonNull ModifyItemRequestDto requestDto) {

        Item item = itemRepository.findById(id)
                .orElseThrow(NotFoundException::new);

        List<Image> itemImagesListCopy = new ArrayList<>(item.getImages());

        injectDtoDataToItem(requestDto, item);

        item.setActive(shouldBeActive(item));

        BindingResult validationResult = validate(item);
        if(validationResult.hasErrors()) {
            throw new InvalidEntityException(validationResult);
        }

        itemImagesListCopy
                .stream()
                .filter(image -> !item.getImages().contains(image))
                .forEach(imageService::delete);

        itemRepository.save(item);
        imageRepository.saveAll(item.getImages());

        return item;
    }

    @Override
    public void updateItemQuantitiesAfterOrder(@NonNull Order order) {

        for(OrderEntry orderEntry : order.getEntries()) {

            Item item = orderEntry.getItem();

            item.updateQuantity(-orderEntry.getQuantity());

            item.setActive(shouldBeActive(item));
            itemRepository.save(item);
        }
    }

    @Override
    public Optional<Item> getById(@NonNull Long id) {
        return itemRepository.findById(id);
    }

    @Override
    public Page<Item> getByFilters(int page, ItemFilterParamsDto filterParams) {
        return itemRepository.findAllByFilterParameters(
                filterParams,
                PageRequest.of(page, ITEMS_PER_PAGE)
        );
    }

    @Override
    public void deleteById(@NonNull Long id) {

        Item item = itemRepository.findById(id).orElseThrow(NotFoundException::new);

        List<OrderEntry> orderEntriesCopy = new ArrayList<>(item.getOrderEntries());

        item.getOrderEntries().clear();

        for(OrderEntry orderEntry : orderEntriesCopy) {

            Order order = orderEntry.getOrder();

            order.getEntries().remove(orderEntry);

            orderEntryRepository.delete(orderEntry);

            if(order.getEntries().isEmpty())
                orderRepository.delete(order);
        }

        itemRepository.delete(item);
        item.getImages().forEach(imageService::delete);
    }

    @Override
    public void save(@NonNull Item item) {
        itemRepository.save(item);
    }

    public BindingResult validate(@NonNull Item item) {
        DataBinder dataBinder = new DataBinder(item);
        dataBinder.addValidators(itemValidator);
        dataBinder.validate();
        return dataBinder.getBindingResult();
    }

    private boolean shouldBeActive(@NonNull Item item) {
        return item.getQuantity() > 0;
    }

    private <T> List<T> getEntitiesReferences(Collection<Long> entitiesIds,
                                              @NonNull JpaRepository<T, Long> repository) {

        List<T> entitiesReferences = new ArrayList<>();

        if(entitiesIds == null) return entitiesReferences;

        for(Long id : entitiesIds)
            entitiesReferences.add(repository.getReferenceById(id));

        return entitiesReferences;
    }

    private void bidirectionalAttachImagesToItem(Collection<Long> imagesIds,
                                                 @NonNull Item item) {

        List<Image> imagesReferences = new ArrayList<>();

        if(imagesIds == null) {
            item.setImages(imagesReferences);
            return;
        }

        imagesIds.stream()
                .map(imageRepository::getReferenceById)
                .peek(imageRef -> imageRef.setItem(item))
                .forEach(imagesReferences::add);

        item.setImages(imagesReferences);
    }

    private void injectDtoDataToItem(@NonNull ModifyItemRequestDto dto, @NonNull Item item) {

        item.setName(dto.getName());
        item.setDescription(dto.getDescription());

        item.setPrice(dto.getPrice());
        item.setVolume(dto.getVolume());
        item.setQuantity(dto.getQuantity());

        bidirectionalAttachImagesToItem(dto.getImagesIds(), item);

        item.setCategories(getEntitiesReferences(dto.getCategoriesIds(), categoryRepository));
    }
}
