package com.siemens.internship.controller;

import com.siemens.internship.dto.ItemDto;
import com.siemens.internship.mapper.ItemMapper;
import com.siemens.internship.service.ItemService;
import com.siemens.internship.model.Item;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    /**
     * Retrieves all items and returns them as DTOs.
     *
     * @return ResponseEntity with list of ItemDTOs and HTTP status
     */
    @GetMapping
    public ResponseEntity<List<ItemDto>> getAllItems() {
        // TODO in the future please add pagination to avoid loading entire database

        List<ItemDto> itemDTOs = itemService.findAll()
                .stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(itemDTOs, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> createItem(@Valid @RequestBody ItemDto itemDTO, BindingResult result) {
        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .toList();
            return ResponseEntity.badRequest().body(errors);
        }

        ItemDto created = itemService.createItem(itemDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }



    @GetMapping("/{id}")
    public ResponseEntity<ItemDto> getItemById(@PathVariable Long id) {
        ItemDto itemDto = itemService.getItemById(id);
        if (itemDto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();  // 404 if item is not found
        }
        return ResponseEntity.ok(itemDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(@PathVariable Long id, @Valid @RequestBody ItemDto itemDTO, BindingResult result) {
        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .toList();
            return ResponseEntity.badRequest().body(errors);  // Return a bad request response with error messages if validation fails
        }

        // Update item and return the updated DTO
        ItemDto updatedItem = itemService.updateItem(id, itemDTO);
        if (updatedItem != null) {
            return ResponseEntity.ok(updatedItem);  // Return the updated item with a 200 OK status
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);  // Return 404 if item is not found
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) throws Exception {
        Optional<Item> item = itemService.findById(id);
        if (item.isPresent()) {
            itemService.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/process")
    public ResponseEntity<List<Item>> processItems() {
        return new ResponseEntity<>(itemService.processItemsAsync(), HttpStatus.OK);
    }
}
