package by.dzarembo.apigateway.service;

import by.dzarembo.apigateway.dto.auth.Role;
import by.dzarembo.apigateway.dto.registration.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class RegistrationService {
    private static final String SYSTEM_USER_ID = "0";
    private static final String SYSTEM_USER_ROLE = Role.ADMIN.name();

    private final WebClient authWebClient;
    private final WebClient userWebClient;

    public RegistrationService(
            @Qualifier("authWebClient") WebClient authWebClient,
            @Qualifier("userWebClient") WebClient userWebClient
    ) {
        this.authWebClient = authWebClient;
        this.userWebClient = userWebClient;
    }

    public Mono<RegisterResponse> register(RegisterRequest registerRequest) {
        UserCreateRequest userCreateRequest = new UserCreateRequest(
                registerRequest.getName(),
                registerRequest.getSurname(),
                registerRequest.getBirthday(),
                registerRequest.getEmail());
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
                    registerRequest.getLogin(),
                    registerRequest.getPassword(),
                    Role.USER
            );
            return authWebClient.post()
                    .uri("/auth/credentials")
                    .bodyValue(createCredentialRequest)
                    .retrieve()
                    .bodyToMono(CredentialResponse.class)
                    .map(credentialResponse -> new RegisterResponse(
                            userResponse.getId(),
                            registerRequest.getEmail(),
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
