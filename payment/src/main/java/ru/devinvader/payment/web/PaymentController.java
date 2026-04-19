package ru.devinvader.payment.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.devinvader.payment.api.PaymentApi;
import ru.devinvader.payment.model.BalanceResponse;
import ru.devinvader.payment.model.PaymentRequest;
import ru.devinvader.payment.model.PaymentResponse;
import ru.devinvader.payment.service.PaymentService;

@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {

    private final PaymentService paymentService;

    @Override
    public Mono<ResponseEntity<BalanceResponse>> getBalance(ServerWebExchange exchange) {
        return paymentService.getBalance()
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<PaymentResponse>> pay(
            @Valid Mono<PaymentRequest> paymentRequest,
            ServerWebExchange exchange
    ) {
        return paymentRequest
                .flatMap(paymentService::pay)
                .map(ResponseEntity::ok);
    }
}
