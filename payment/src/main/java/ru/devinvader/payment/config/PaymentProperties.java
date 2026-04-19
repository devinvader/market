package ru.devinvader.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Конфигурация из application.yaml, чтобы не искать по всем классам потом
 */
@ConfigurationProperties(prefix = "payment")
public record PaymentProperties(long initialBalance) {
}
