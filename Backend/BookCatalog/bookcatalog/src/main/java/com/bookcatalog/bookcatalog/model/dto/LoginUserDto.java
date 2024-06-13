package com.bookcatalog.bookcatalog.model.dto;

import com.bookcatalog.bookcatalog.helpers.validation.UsernameOrEmailRequired;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Data
@UsernameOrEmailRequired
public class LoginUserDto {
    
    @Size(min = 2, max = 100, message = "The length of full name must be between 2 and 100 characters.")
    private String username;
    @Email(message = "The email address is invalid.", flags = {Pattern.Flag.CASE_INSENSITIVE})
    private String email;
    @NotEmpty(message = "Password is required")
    @Pattern(regexp = "(?=.*[a-z])(?=.*d)(?=.*[@#$%])(?=.*[A-Z]).{6,16}", flags = {Pattern.Flag.CASE_INSENSITIVE}, message = "The given password does not match the rules")
    private String password;

    public LoginUserDto(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
