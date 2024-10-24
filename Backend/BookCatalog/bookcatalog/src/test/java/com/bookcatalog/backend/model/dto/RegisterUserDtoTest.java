package com.bookcatalog.backend.model.dto;

import com.bookcatalog.backend.model.Role;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegisterUserDtoTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterEach
    public void tearDown() {
        validator = null;
    }

    @Test
    public void testObjectCreationAndGettersSetters() {
        // Create an instance of RegisterUserDto
        RegisterUserDto registerUserDto = new RegisterUserDto("testUser", "test@example.com", "Password1@", Role.READER);

        // Validate using getters
        assertEquals("testUser", registerUserDto.getUsername());
        assertEquals("test@example.com", registerUserDto.getEmail());
        assertEquals("Password1@", registerUserDto.getPassword());
        assertEquals(Role.READER, registerUserDto.getRole());

        // Set new values using setters
        registerUserDto.setUsername("newUser");
        registerUserDto.setEmail("new@example.com");
        registerUserDto.setPassword("NewPassword1@");
        registerUserDto.setRole(Role.ADMIN);

        // Validate using getters
        assertEquals("newUser", registerUserDto.getUsername());
        assertEquals("new@example.com", registerUserDto.getEmail());
        assertEquals("NewPassword1@", registerUserDto.getPassword());
        assertEquals(Role.ADMIN, registerUserDto.getRole());
    }
/*
    @Test
    public void testUsernameNotEmptyValidation() {
        RegisterUserDto registerUserDto = new RegisterUserDto("", "test@example.com", "Password1@", Role.READER);

        Set<ConstraintViolation<RegisterUserDto>> violations = validator.validate(registerUserDto);
        assertFalse(violations.isEmpty());

        ConstraintViolation<RegisterUserDto> violation = violations.iterator().next();
        assertEquals("Username is required", violation.getMessage());
    }

    @Test
    public void testEmailNotEmptyValidation() {
        RegisterUserDto registerUserDto = new RegisterUserDto("testUser", "", "Password1@", Role.READER);

        Set<ConstraintViolation<RegisterUserDto>> violations = validator.validate(registerUserDto);
        assertFalse(violations.isEmpty());

        ConstraintViolation<RegisterUserDto> violation = violations.iterator().next();
        assertEquals("Email is required", violation.getMessage());
    }

    @Test
    public void testEmailPatternValidation() {
        RegisterUserDto registerUserDto = new RegisterUserDto("testUser", "invalid-email", "Password1@", Role.READER);

        Set<ConstraintViolation<RegisterUserDto>> violations = validator.validate(registerUserDto);
        assertFalse(violations.isEmpty());

        ConstraintViolation<RegisterUserDto> violation = violations.iterator().next();
        assertEquals("The email address is invalid.", violation.getMessage());
    }

    @Test
    public void testPasswordNotEmptyValidation() {
        RegisterUserDto registerUserDto = new RegisterUserDto("testUser", "test@example.com", "", Role.READER);

        Set<ConstraintViolation<RegisterUserDto>> violations = validator.validate(registerUserDto);
        assertFalse(violations.isEmpty());

        ConstraintViolation<RegisterUserDto> violation = violations.iterator().next();
        assertEquals("Password is required", violation.getMessage());
    }

    @Test
    public void testPasswordPatternValidation() {
        RegisterUserDto registerUserDto = new RegisterUserDto("testUser", "test@example.com", "password", Role.READER);

        Set<ConstraintViolation<RegisterUserDto>> violations = validator.validate(registerUserDto);
        assertFalse(violations.isEmpty());

        ConstraintViolation<RegisterUserDto> violation = violations.iterator().next();
        assertEquals("The given password does not match the rules", violation.getMessage());
    }

    @Test
    public void testRoleNotEmptyValidation() {
        RegisterUserDto registerUserDto = new RegisterUserDto("testUser", "test@example.com", "Password1@", null);

        Set<ConstraintViolation<RegisterUserDto>> violations = validator.validate(registerUserDto);
        assertFalse(violations.isEmpty());

        ConstraintViolation<RegisterUserDto> violation = violations.iterator().next();
        assertEquals("Role is required", violation.getMessage());
    }
*/
}