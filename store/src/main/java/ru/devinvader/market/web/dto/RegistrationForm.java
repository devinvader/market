package ru.devinvader.market.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistrationForm(
        @NotBlank(message = "Имя пользователя не может быть пустым")
        @Pattern(regexp = "^[a-zA-Zа-яА-ЯёЁ0-9_]+$",
                message = "Имя пользователя может содержать только буквы, цифры и подчеркивания")
        String username,

        @Size(min = 3, message = "Пароль должен содержать минимум 3 символа")
        @Pattern(regexp = "^[a-zA-Zа-яА-ЯёЁ0-9_]+$",
                message = "Имя пользователя может содержать только буквы, цифры и подчеркивания")
        String password,

        String confirmPassword
) {}
