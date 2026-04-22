package by.dzarembo.apigateway.dto.registration;

import by.dzarembo.apigateway.dto.auth.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CredentialResponse {
    private Long id;
    private Long userId;
    private String login;
    private Role role;
    private Boolean active;
}