package ru.devinvader.market.integration.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockAuthentication;

class LoginControllerIntegrationTest extends IntegrationBaseTest {

    @Autowired
    private WebTestClient webTestClient;

    private static final AnonymousAuthenticationToken anonymousToken = new AnonymousAuthenticationToken(
            "anonymous", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
    );

    @Test
    void getLogin_shouldReturnLoginView() {
        webTestClient.mutateWith(mockAuthentication(anonymousToken))
                .get().uri("/login")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body, containsString("Вход")));
    }

    @Test
    void getLoginWithRegisteredParam_shouldShowSuccessMessage() {
        webTestClient.mutateWith(mockAuthentication(anonymousToken))
                .get().uri("/login?registered")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body, containsString("Регистрация прошла успешно")));
    }

    @Test
    void getLoginWithErrorParam_shouldShowErrorMessage() {
        webTestClient.mutateWith(mockAuthentication(anonymousToken))
                .get().uri("/login?error")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body, containsString("Неверное имя пользователя или пароль")));
    }

}
