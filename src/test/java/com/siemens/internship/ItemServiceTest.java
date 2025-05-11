package com.siemens.internship;

import com.siemens.internship.dto.ItemDto;
import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import com.siemens.internship.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    public void setUp() {
        item = Item.builder()
                .id(1L)
                .name("Test Item")
                .email("test@example.com")
                .description("A test item")
                .status("AVAILABLE")
                .build();

        itemDto = ItemDto.builder()
                .name("Test Item")
                .email("test@example.com")
                .description("A test item")
                .status("AVAILABLE")
                .build();
    }


    @Test
    public void testCreateItem() {
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.createItem(itemDto);

        assertNotNull(result);
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getEmail(), result.getEmail());

        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    public void testGetItemById_ItemExists() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ItemDto result = itemService.getItemById(1L);

        assertNotNull(result);
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getEmail(), result.getEmail());
    }

    @Test
    public void testGetItemById_ItemNotFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        ItemDto result = itemService.getItemById(1L);

        assertNull(result);
    }

    @Test
    public void testUpdateItem() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto updatedItem = new ItemDto(1L, "Updated Item", "Updated Description", "SOLD", "updated@example.com");
        ItemDto result = itemService.updateItem(1L, updatedItem);

        assertNotNull(result);
        assertEquals(updatedItem.getName(), result.getName());
        assertEquals(updatedItem.getEmail(), result.getEmail());
    }

    @Test
    public void testUpdateItem_ItemNotFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        ItemDto updatedItem = new ItemDto(1L, "Updated Item", "Updated Description", "SOLD", "updated@example.com");
        ItemDto result = itemService.updateItem(1L, updatedItem);

        assertNull(result);
    }

    @Test
    public void testDeleteItem_Success() throws Exception {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        itemService.deleteById(1L);

        verify(itemRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteItem_ItemNotFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () -> itemService.deleteById(1L));
        assertEquals("Item not found with id: 1", exception.getMessage());
    }

    @Test
    public void testProcessItemsAsync() throws Exception {
        // Arrange
        List<Long> ids = List.of(1L, 2L);

        Item item1 = Item.builder()
                .id(1L)
                .name("Item 1")
                .email("item1@example.com")
                .description("Desc 1")
                .status("NEW")
                .build();

        Item item2 = Item.builder()
                .id(2L)
                .name("Item 2")
                .email("item2@example.com")
                .description("Desc 2")
                .status("NEW")
                .build();

        when(itemRepository.findAllIds()).thenReturn(ids);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CompletableFuture<List<Item>> future = itemService.processItemsAsync();
        List<Item> result = future.get(); // Blocking for test

        // Assert
        assertEquals(2, result.size());
        for (Item item : result) {
            assertEquals("PROCESSED", item.getStatus());
        }

        verify(itemRepository, times(1)).findAllIds();
        verify(itemRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).findById(2L);
        verify(itemRepository, times(2)).save(any(Item.class));
    }
}
