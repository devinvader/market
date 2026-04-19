package ru.devinvader.payment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;
import ru.devinvader.payment.config.PaymentProperties;
import ru.devinvader.payment.model.PaymentRequest;
import ru.devinvader.payment.model.PaymentResponse;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {

    private static final long INITIAL_BALANCE = 10_000L;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(new PaymentProperties(INITIAL_BALANCE));
        paymentService.init();
    }

    @Test
    void getBalance_returnsInitialBalance() {
        // when
        StepVerifier.create(paymentService.getBalance())
        // then
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals(INITIAL_BALANCE, response.getBalance());
                })
                .verifyComplete();
    }

    @Test
    void pay_sufficientBalance_returnsSuccess() {
        // given
        long amount = 3000L;

        // when
        StepVerifier.create(paymentService.pay(new PaymentRequest().amount(amount)))
        // then
                .assertNext(response -> {
                    assertTrue(response.getSuccess());
                    assertEquals(INITIAL_BALANCE - amount, response.getNewBalance());
                    assertNotNull(response.getMessage());
                })
                .verifyComplete();
    }

    @Test
    void pay_insufficientBalance_returnsFailure() {
        // given
        long amount = INITIAL_BALANCE + 1;

        // when
        StepVerifier.create(paymentService.pay(new PaymentRequest().amount(amount)))
        // then
                .assertNext(response -> {
                    assertFalse(response.getSuccess());
                    assertEquals(INITIAL_BALANCE, response.getNewBalance());
                    assertNotNull(response.getMessage());
                })
                .verifyComplete();
    }

    @Test
    void pay_exactBalance_returnsSuccessWithZeroBalance() {
        // given и when
        StepVerifier.create(paymentService.pay(new PaymentRequest().amount(INITIAL_BALANCE)))
                .assertNext(response -> {
                    assertTrue(response.getSuccess());
                    assertEquals(0L, response.getNewBalance());
                })
                .verifyComplete();

        // then
        StepVerifier.create(paymentService.pay(new PaymentRequest().amount(1L)))
                .assertNext(response -> assertFalse(response.getSuccess()))
                .verifyComplete();
    }

    @Test
    void pay_balanceUpdatedAfterSuccessfulPayment() {
        // given
        long firstPayment = 4000L;
        long secondPayment = 3000L;
        // when
        paymentService.pay(new PaymentRequest().amount(firstPayment)).block();
        // then
        StepVerifier.create(paymentService.getBalance())
                .assertNext(response -> assertEquals(INITIAL_BALANCE - firstPayment, response.getBalance()))
                .verifyComplete();

        StepVerifier.create(paymentService.pay(new PaymentRequest().amount(secondPayment)))
                .assertNext(response -> {
                    assertTrue(response.getSuccess());
                    assertEquals(INITIAL_BALANCE - firstPayment - secondPayment, response.getNewBalance());
                })
                .verifyComplete();
    }

    @Test
    void pay_concurrentPayments_balanceIsConsistent() throws InterruptedException {
        // given
        int threadCount = 10;
        long paymentAmount = 1000L;
        // 10 потоков * 1000 = 10000 = INITIAL_BALANCE, все должны пройти
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    PaymentResponse response = paymentService
                            .pay(new PaymentRequest().amount(paymentAmount))
                            .block();
                    if (Boolean.TRUE.equals(response.getSuccess())) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // then
        assertEquals(threadCount, successCount.get());

        StepVerifier.create(paymentService.getBalance())
                .assertNext(response -> assertEquals(0L, response.getBalance()))
                .verifyComplete();
    }
}
