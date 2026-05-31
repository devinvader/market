package ru.devinvader.market.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.XorServerCsrfTokenRequestAttributeHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.net.URI;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        var csrfHandler = new XorServerCsrfTokenRequestAttributeHandler();
        csrfHandler.setTokenFromMultipartDataEnabled(true);

        return http
                .authorizeExchange(exchanges -> exchanges
                        // наверное, стоило сделать это через PreAuthorize в контроллерах, но
                        // мне такое больше понравилось
                        .pathMatchers(HttpMethod.GET, "/", "/items", "/items/{id}", "/items/{id}/image").permitAll()
                        .pathMatchers("/login", "/logout", "/register").permitAll()
                        .pathMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        .pathMatchers("/admin/**").hasRole("ADMIN")
                        .anyExchange().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((exchange, authentication) ->
                                exchange.getExchange().getSession()
                                        .flatMap(WebSession::invalidate)
                                        .then(Mono.fromRunnable(() -> {
                                            exchange.getExchange().getResponse().setStatusCode(HttpStatus.FOUND);
                                            exchange.getExchange().getResponse().getHeaders().setLocation(URI.create("/login?logout"));
                                        }))
                        )
                )
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler((exchange, denied) ->
                                Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied", denied)))
                )
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(csrfHandler)
                )
                .build();
    }
}
