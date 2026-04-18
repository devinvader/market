package ru.devinvader.market.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.devinvader.market.service.dto.BalanceResponse;
import ru.devinvader.market.service.dto.PaymentRequest;
import ru.devinvader.market.service.dto.PaymentResponse;
import static ru.devinvader.market.utils.ClientConstants.BALANCE_SERVICE_UNAVAILABLE;

@Slf4j
@Service
public class PaymentClientService {

    private final WebClient webClient;

    public PaymentClientService(@Qualifier("paymentWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Получает текущий баланс с сервиса платежей.
     * При ошибке возвращает {@link ru.devinvader.market.utils.ClientConstants#BALANCE_SERVICE_UNAVAILABLE}.
     */
    public Mono<Long> getBalance() {
        return webClient.get()
                .uri("/api/payment/balance")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(BalanceResponse.class)
                .map(BalanceResponse::balance)
                .onErrorResume(e -> {
                    log.warn("Payment service unavailable: {}", e.getMessage());
                    return Mono.just(BALANCE_SERVICE_UNAVAILABLE);
                });
    }

    /**
     * Проверяет, доступен ли сервис платежей (баланс != SERVICE_UNAVAILABLE_BALANCE).
     */
    public Mono<Boolean> isServiceAvailable() {
        return getBalance().map(b -> b != BALANCE_SERVICE_UNAVAILABLE);
    }

    /**
     * Выполняет платёж (списание суммы с баланса).
     * При ошибке подключения возвращает PaymentResponse с success=false и сообщением об ошибке.
     */
    public Mono<PaymentResponse> pay(long amount) {
        return webClient.post()
                .uri("/api/payment/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new PaymentRequest(amount))
                .retrieve()
                .bodyToMono(PaymentResponse.class)
                .onErrorResume(e -> {
                    log.error("Payment service unavailable: {}", e.getMessage());
                    return Mono.just(new PaymentResponse(false, BALANCE_SERVICE_UNAVAILABLE, "Сервис платежей недоступен"));
                });
    }
}
