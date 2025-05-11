package com.siemens.internship.service;

import com.siemens.internship.dto.ItemDto;
import com.siemens.internship.mapper.ItemMapper;
import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;
    private static ExecutorService executor = Executors.newFixedThreadPool(10);
    private List<Item> processedItems = new ArrayList<>();
    private int processedCount = 0;

    /**
     * Retrieves all items from the database.
     *
     * This method abstracts the database interaction and returns raw entity objects.
     * Conversion to DTOs should be handled at the controller layer.
     *
     * @return List of all Item entities
     */
    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    /**
     * Creates a new item in the system.
     * Handles DTO to entity conversion and persistence.
     *
     * @param dto DTO representing the item to create
     * @return DTO of the saved item
     */
    public ItemDto createItem(ItemDto dto) {
        Item entity = ItemMapper.toEntity(dto);
        Item saved = itemRepository.save(entity);
        return ItemMapper.toDto(saved);
    }


    /**
     * Retrieves an item by its ID and returns it as a DTO.
     * If the item is not found, it returns null.
     *
     * @param id The ID of the item to retrieve.
     * @return An ItemDto if the item exists, otherwise null.
     */
    public ItemDto getItemById(Long id) {
        Optional<Item> item = itemRepository.findById(id);
        return item.map(ItemMapper::toDto).orElse(null);
    }


    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }


    /**
     * Your Tasks
     * Identify all concurrency and asynchronous programming issues in the code
     * Fix the implementation to ensure:
     * All items are properly processed before the CompletableFuture completes
     * Thread safety for all shared state
     * Proper error handling and propagation
     * Efficient use of system resources
     * Correct use of Spring's @Async annotation
     * Add appropriate comments explaining your changes and why they fix the issues
     * Write a brief explanation of what was wrong with the original implementation
     *
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     */
    @Async
    public List<Item> processItemsAsync() {

        List<Long> itemIds = itemRepository.findAllIds();

        for (Long id : itemIds) {
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(100);

                    Item item = itemRepository.findById(id).orElse(null);
                    if (item == null) {
                        return;
                    }

                    processedCount++;

                    item.setStatus("PROCESSED");
                    itemRepository.save(item);
                    processedItems.add(item);

                } catch (InterruptedException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }, executor);
        }

        return processedItems;
    }

}

