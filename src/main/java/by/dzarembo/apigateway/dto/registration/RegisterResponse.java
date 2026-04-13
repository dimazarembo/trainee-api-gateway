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
public class RegisterResponse {
    private Long userId;
    private String email;
    private String login;
    private Role role;
}
