package by.dzarembo.apigateway.dto.auth;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ValidationTokenResponse {
    private Boolean valid;
    private Long userId;
    private String role;
    private String tokenType;
}
