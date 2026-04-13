package by.dzarembo.apigateway.controller;

import by.dzarembo.apigateway.dto.auth.Role;
import by.dzarembo.apigateway.dto.registration.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
public class RegistrationController {
    private static final String SYSTEM_USER_ID = "0";
    private static final String SYSTEM_USER_ROLE = Role.ADMIN.name();

    private final WebClient authWebClient;
    private final WebClient userWebClient;

    public RegistrationController(
            @Qualifier("authWebClient") WebClient authWebClient,
            @Qualifier("userWebClient") WebClient userWebClient
    ) {
        this.authWebClient = authWebClient;
        this.userWebClient = userWebClient;
    }

    @PostMapping("/register")
    public Mono<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserCreateRequest userCreateRequest = new UserCreateRequest(
                request.getName(),
                request.getSurname(),
                request.getBirthday(),
                request.getEmail());
        var userMono = userWebClient.post()
                .uri("/users")
                .header("X-User-Id", SYSTEM_USER_ID)
                .header("X-User-Role", SYSTEM_USER_ROLE)
                .bodyValue(userCreateRequest)
                .retrieve()
                .bodyToMono(UserResponse.class);

        return userMono.flatMap(userResponse -> {
            CreateCredentialRequest createCredentialRequest = new CreateCredentialRequest(
                    userResponse.getId(),
                    request.getLogin(),
                    request.getPassword(),
                    Role.USER
            );
            return authWebClient.post()
                    .uri("/auth/credentials")
                    .bodyValue(createCredentialRequest)
                    .retrieve()
                    .bodyToMono(CredentialResponse.class)
                    .map(credentialResponse -> new RegisterResponse(
                            userResponse.getId(),
                            request.getEmail(),
                            credentialResponse.getLogin(),
                            credentialResponse.getRole()
                    )).onErrorResume(ex -> deleteUser(userResponse.getId()).then(Mono.error(ex)));
        });
    }

    private Mono<Void> deleteUser(Long userId) {
        return userWebClient.delete().uri("/users/{id}", userId)
                .header("X-User-Id", SYSTEM_USER_ID)
                .header("X-User-Role", SYSTEM_USER_ROLE)
                .retrieve().toBodilessEntity().then();
    }


}
