package ru.devinvader.market.service.dto;

public record PaymentResponse(boolean success, long newBalance, String message) {
}
