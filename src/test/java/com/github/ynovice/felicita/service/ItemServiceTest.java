package com.github.ynovice.felicita.service;

import com.github.ynovice.felicita.Application;
import com.github.ynovice.felicita.model.dto.request.ModifyItemRequestDto;
import com.github.ynovice.felicita.model.entity.Item;
import com.github.ynovice.felicita.repository.ItemRepository;
import com.github.ynovice.felicita.validator.ItemValidator;
import lombok.NonNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;

import java.util.*;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = Application.class
)
@ActiveProfiles("test")
@TestPropertySource("/application-test.yml")
@AutoConfigureMockMvc
public class ItemServiceTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemValidator itemValidator;

    @Test
    public void createValidItemTest() {

        ModifyItemRequestDto requestDto = validCreateItemRequestDto();
        Item item = itemService.createItem(requestDto);

        // id
        assertNotNull(item.getId());

        // name
        assertNotNull(item.getName());
        assertEquals(requestDto.getName(), item.getName());
        // description
        assertNotNull(item.getDescription());
        assertEquals(requestDto.getDescription(), item.getDescription());
        // price
        assertNotNull(item.getPrice());
        assertEquals(requestDto.getPrice(), item.getPrice());

        // price
        assertNotNull(item.getImages());
        assertEquals(requestDto.getImagesIds().size(), item.getImages().size());

        // categories
        assertNotNull(item.getCategories());
        assertEquals(requestDto.getCategoriesIds().size(), item.getCategories().size());

        // colors
        assertNotNull(item.getColors());
        assertEquals(requestDto.getColorsIds().size(), item.getColors().size());

        // createdAt
        assertNotNull(item.getCreatedAt());

        // active
        assertEquals(shouldBeActive(requestDto), item.getActive());

        // sizesQuantities
        assertEquals(requestDto.getQuantity(), item.getQuantity());

        // cartEntries
        assertNotNull(item.getCartEntries());
        assertTrue(item.getCartEntries().isEmpty());

        // testing persistence
        Optional<Item> repositoryItemOptional = itemRepository.findById(item.getId());
        assertTrue(repositoryItemOptional.isPresent());
        assertEquals(item, repositoryItemOptional.get());

        // testing validation
        assertFalse(validate(item).hasErrors());
    }

    private boolean shouldBeActive(@NonNull ModifyItemRequestDto modifyItemRequestDto) {
        return modifyItemRequestDto.getQuantity() > 0;
    }

    private ModifyItemRequestDto validCreateItemRequestDto() {

        ModifyItemRequestDto requestDto = new ModifyItemRequestDto();

        requestDto.setName("Adidas Sneakers");
        requestDto.setDescription("Item description");
        requestDto.setImagesIds(Collections.emptyList());
        requestDto.setCategoriesIds(Collections.emptyList());

        requestDto.setColorsIds(new ArrayList<>());

        requestDto.setPrice(5000);

        requestDto.setQuantity(0);

        return requestDto;
    }

    private BindingResult validate(@NonNull Item item) {
        DataBinder dataBinder = new DataBinder(item);
        dataBinder.addValidators(itemValidator);
        dataBinder.validate(new Object());
        return dataBinder.getBindingResult();
    }
}
