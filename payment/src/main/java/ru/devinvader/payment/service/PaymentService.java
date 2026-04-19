package ru.devinvader.payment.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.devinvader.payment.config.PaymentProperties;
import ru.devinvader.payment.model.BalanceResponse;
import ru.devinvader.payment.model.PaymentRequest;
import ru.devinvader.payment.model.PaymentResponse;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentProperties properties;
    private final AtomicLong balance = new AtomicLong();

    @PostConstruct
    public void init() {
        balance.set(properties.initialBalance());
    }

    public Mono<BalanceResponse> getBalance() {
        return Mono.fromSupplier(() -> {
            long current = balance.get();
            return new BalanceResponse().balance(current);
        });
    }

    public Mono<PaymentResponse> pay(PaymentRequest request) {
        return Mono.fromSupplier(() -> {
            long amount = request.getAmount();
            log.debug("Payment requested for amount: {}", amount);
            // для потокобезопасности
            while (true) {
                long current = balance.get();
                long after = current - amount;

                if (after < 0) {
                    return new PaymentResponse()
                            .success(false)
                            .newBalance(current)
                            .message("Недостаточно средств на счёте");
                }

                // Если не удалось установить баланс - ещё раз прогоняет
                if (balance.compareAndSet(current, after)) {
                    log.info("Payment successful: {} payed, new balance: {}", amount, after);
                    return new PaymentResponse()
                            .success(true)
                            .newBalance(after)
                            .message("Оплата прошла успешно");
                }
            }
        });
    }
}
