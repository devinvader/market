package ru.devinvader.market.integration.controller;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Мок пользователя для взаимодействия с эндпоинтами, в которых требуется аутентификация
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockMarketUserSecurityContextFactory.class)
public @interface WithMockMarketUser {
    long userId() default 100;

    String username() default "testuser";

    String role() default "USER";
}
