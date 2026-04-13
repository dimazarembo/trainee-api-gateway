package by.dzarembo.apigateway.dto.registration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String surname;
    @Past
    private LocalDate birthday;
    @Email
    private String email;
    @NotBlank
    private String login;
    @NotBlank
    @Size(min = 6, max = 25)
    private String password;
}
