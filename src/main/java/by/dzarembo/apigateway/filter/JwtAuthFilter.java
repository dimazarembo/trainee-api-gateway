package by.dzarembo.apigateway.filter;

import by.dzarembo.apigateway.dto.auth.ValidationTokenRequest;
import by.dzarembo.apigateway.dto.auth.ValidationTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter {

    private final WebClient authWebClient;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();
        if (isPublicEndpoint(method, path)) {
            return chain.filter(exchange);
        }

        String authorizationHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        String token = extractBearerToken(authorizationHeader);

        if (token == null) {
            return unauthorized(exchange);
        }
        return authWebClient.post().uri("/auth/validate")
                .bodyValue(new ValidationTokenRequest(token))
                .retrieve()
                .bodyToMono(ValidationTokenResponse.class)
                .flatMap(response -> {
                    var mutatedRequest = exchange.getRequest().mutate()
                            .header("X-User-Id", String.valueOf(response.getUserId()))
                            .header("X-User-Role", response.getRole())
                            .build();

                    var mutatedExchange = exchange.mutate()
                            .request(mutatedRequest)
                            .build();

                    return chain.filter(mutatedExchange);
                }).onErrorResume(throwable -> unauthorized(exchange));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private boolean isPublicEndpoint(HttpMethod method, String path) {
        return HttpMethod.POST.equals(method) &&
                ("/auth/login".equals(path) || "/register".equals(path));
    }

    private String extractBearerToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }

}
