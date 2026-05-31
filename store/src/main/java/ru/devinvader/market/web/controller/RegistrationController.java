package ru.devinvader.market.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Mono;
import ru.devinvader.market.domain.User;
import ru.devinvader.market.repository.UserRepository;
import ru.devinvader.market.web.dto.RegistrationForm;

@Controller
@RequiredArgsConstructor
public class RegistrationController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public Mono<String> showRegistrationForm() {
        return Mono.just("register");
    }

    @PostMapping("/register")
    public Mono<String> register(
            @Valid @ModelAttribute RegistrationForm form,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            String error = bindingResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .findFirst()
                    .orElse("Ошибка валидации");
            model.addAttribute("error", error);
            return Mono.just("register");
        }

        if (!form.password().equals(form.confirmPassword())) {
            model.addAttribute("error", "Пароли не совпадают");
            return Mono.just("register");
        }

        return userRepository.findByUsername(form.username().trim())
                .flatMap(existingUser -> {
                    model.addAttribute("error", "Пользователь с таким именем уже существует");
                    return Mono.just("register");
                })
                .switchIfEmpty(Mono.defer(() -> {
                    User user = new User();
                    user.setUsername(form.username().trim());
                    user.setPassword(passwordEncoder.encode(form.password()));
                    user.setRole("ROLE_USER");
                    return userRepository.save(user)
                            .then(Mono.just("redirect:/login?registered"));
                }));
    }
}
