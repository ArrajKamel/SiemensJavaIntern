package com.siemens.internship.service;

import com.siemens.internship.dto.ItemDto;
import com.siemens.internship.mapper.ItemMapper;
import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    @Autowired
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }


    /**
     * Retrieves all items from the database.
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



    public void deleteById(Long id) throws Exception {
        // TODO for future verify permissions if the current user is allowed to delete this item.
        Optional<Item> item = itemRepository.findById(id);
        if (item.isPresent()) {
            itemRepository.deleteById(id);
        } else {
            // TODO replace with custom exception "ResourceNotFoundException"
            throw new Exception("Item not found with id: " + id);
        }
    }

    /**
     * Updates an existing item identified by the given ID.
     * <p>
     * This method checks if the item exists in the database. If found, it updates the item's details
     * (name, description, status, and email) and saves the updated item. If the item is not found,
     * it returns {@code null}.
     * </p>
     *
     * @param id The ID of the item to be updated.
     * @param itemDTO The DTO containing the updated information for the item.
     * @return The updated {@link ItemDto} if the item is found and successfully updated,
     *         {@code null} if the item does not exist.
     */
    public ItemDto updateItem(Long id, ItemDto itemDTO) {
        // TODO for future verify permissions if the current user is allowed to update this item.

        Optional<Item> itemOptional = itemRepository.findById(id);

        if (itemOptional.isEmpty()) {
            return null;
        }

        Item item = itemOptional.get();
        item.setName(itemDTO.getName());
        item.setDescription(itemDTO.getDescription());
        item.setStatus(itemDTO.getStatus());
        item.setEmail(itemDTO.getEmail());

        item = itemRepository.save(item);
        return ItemMapper.toDto(item);
    }


    /**
     * Processes all items asynchronously by updating their status to "PROCESSED".
     * <p>
     * This method runs in the background using Spring's default async executor.
     * It retrieves all item IDs, processes each in parallel using CompletableFutures,
     * and returns a CompletableFuture that completes only after all items are processed.
     * <p>
     * Note: A custom Executor can be injected later for better control over thread management.
     *
     * @return a CompletableFuture that eventually contains the list of successfully processed items
     */
    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {

        List<Long> itemIds = itemRepository.findAllIds();

        List<CompletableFuture<Item>> futures = itemIds.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> {
                    try {
                        Thread.sleep(100);

                        Optional<Item> optionalItem = itemRepository.findById(id);
                        if (optionalItem.isEmpty()) return null;

                        Item item = optionalItem.get();
                        item.setStatus("PROCESSED");
                        return itemRepository.save(item);

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return null;
                    } catch (Exception e) {
                        System.err.println("Error processing item " + id + ": " + e.getMessage());
                        return null;
                    }
                } , executor/* TODO: Use custom executor for better control in production */))
                .toList();

        CompletableFuture<Void> allDone = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );

        return allDone.thenApply(v ->
                futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
        );
    }
}

