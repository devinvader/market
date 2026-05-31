package ru.devinvader.market.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.devinvader.market.repository.UserRepository;
import ru.devinvader.market.service.dto.MarketUserDetails;

@Service
@RequiredArgsConstructor
public class MarketUserDetailsService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found: " + username)))
                .map(user -> new MarketUserDetails(
                        user.getId(),
                        user.getUsername(),
                        user.getPassword(),
                        user.getRole()
                ));
    }
}
