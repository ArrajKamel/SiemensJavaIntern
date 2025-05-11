# Refactored Method: getAllItems()

## Purpose

Improve API design and data encapsulation by returning DTOs instead of raw JPA entities.

## What Was Changed

- Introduced a `ItemDTO` class.
- Refactored the controller to convert entities to DTOs before returning the response.
- Kept the service method `findAll()` returning raw `Item` entities.
- Added appropriate JavaDoc documentation.

## Why This Matters

- Reduces coupling between layers.
- Protects internal representation of entities.
- Makes API responses more predictable and maintainable.

# Refactoring - Create Item Method

## Summary of Changes

### 1. Controller Changes:

- **DTO Usage**:

  - The `@RequestBody` of the controller now expects an `ItemDto` instead of the raw `Item` entity.
  - This ensures better separation of concerns, encapsulating the data sent by the client.
- **Email Validation**:

  - This prevents invalid email data from being passed to the service or database layer.
- **Error Handling**:

  - We now handle binding errors in the controller. If any validation fails (e.g., invalid email format or missing required fields), the controller returns a list of specific error messages with a `BAD_REQUEST` status code (`400`).
  - This provides the client with a clear explanation of what went wrong.
- **BindingResult**:

  - The `BindingResult` is used to capture any validation errors after the `@Valid` annotation has processed the `ItemDto`. It is checked before performing any further actions to prevent invalid data from being saved.

### 2. Service Changes:

- **Method Signature Update**:

  - The `createItem` method in the service layer has been updated to accept an `ItemDto` instead of an `Item`. This allows the controller to pass a validated DTO to the service layer.
- **Entity Conversion**:

  - This ensures that only valid data (after being validated in the controller) gets passed to the entity.

### 3. ItemDto:

- **DTO Fields**:

  - The `ItemDto` class has been added to handle the data transfer between the controller and the client.

## Future Improvements

- **Exception Handling**: Consider adding a global exception handler to handle validation errors more effectively.
- **Pagination**: Implement pagination to improve API performance when retrieving large numbers of items in the future.



# Refactoring - Get Item by ID Method

## Summary of Changes

### 1. Controller Changes:

- **Improved Error Handling**:

  - The controller method for retrieving an item by ID was updated to return a `404 NOT_FOUND` status code when the item is not found in the database.
  - Previously, it returned a `204 NO_CONTENT` status, which was not semantically correct when the item was missing.
- **Better Response Handling**:

  - The `Optional<Item>` is now used to check if the item exists before attempting to return it. If the item is present, it returns a `200 OK` status along with the item.
  - If the item is not found, a `404 NOT_FOUND` response is returned to indicate that the requested resource could not be found.

## Future Improvements:

- **Error Handling**: Consider adding a global exception handler for improved consistency and error management across the application.

---

## Commit Message:
