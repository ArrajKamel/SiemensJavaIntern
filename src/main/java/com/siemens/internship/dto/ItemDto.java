package com.siemens.internship.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
        private Long id;
        @NotBlank(message = "Name is required")
        private String name;
        private String description;
        private String status;
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;
}
