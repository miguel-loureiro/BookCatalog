package com.bookcatalog.bookcatalog.model.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LoginUserDtoTest {

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
    public void testNoArgsConstructor() {
        LoginUserDto loginUserDto = new LoginUserDto();

        assertNull(loginUserDto.getUsername());
        assertNull(loginUserDto.getEmail());
        assertNull(loginUserDto.getPassword());
    }

    @Test
    public void testAllArgsConstructor() {
        LoginUserDto loginUserDto = new LoginUserDto("john_doe", "john.doe@example.com", "Password1@");

        assertEquals("john_doe", loginUserDto.getUsername());
        assertEquals("john.doe@example.com", loginUserDto.getEmail());
        assertEquals("Password1@", loginUserDto.getPassword());
    }

    @Test
    public void testSettersAndGetters() {
        LoginUserDto loginUserDto = new LoginUserDto();

        loginUserDto.setUsername("jane_doe");
        loginUserDto.setEmail("jane.doe@example.com");
        loginUserDto.setPassword("Password1@");

        assertEquals("jane_doe", loginUserDto.getUsername());
        assertEquals("jane.doe@example.com", loginUserDto.getEmail());
        assertEquals("Password1@", loginUserDto.getPassword());
    }

    @Test
    public void testUsernameSizeValidation() {
        LoginUserDto loginUserDto = new LoginUserDto();
        loginUserDto.setUsername("A");

        Set<ConstraintViolation<LoginUserDto>> violations = validator.validate(loginUserDto);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testEmptyEmailValidation() {

        LoginUserDto loginUserDto = new LoginUserDto();
        loginUserDto.setEmail("invalid-email");

        Set<ConstraintViolation<LoginUserDto>> violations = validator.validate(loginUserDto);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testInvalidEmailValidation() {
        LoginUserDto loginUserDto = new LoginUserDto();
        loginUserDto.setEmail("someemail");

        Set<ConstraintViolation<LoginUserDto>> violations = validator.validate(loginUserDto);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testPasswordNotEmptyValidation() {
        LoginUserDto loginUserDto = new LoginUserDto();
        loginUserDto.setPassword("");

        Set<ConstraintViolation<LoginUserDto>> violations = validator.validate(loginUserDto);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testPasswordPatternValidation() {
        LoginUserDto loginUserDto = new LoginUserDto();
        loginUserDto.setPassword("password");

        Set<ConstraintViolation<LoginUserDto>> violations = validator.validate(loginUserDto);
        assertFalse(violations.isEmpty());

        ConstraintViolation<LoginUserDto> violation = violations.iterator().next();
        assertEquals("The given password does not match the rules", violation.getMessage());
    }

    @Test
    public void testValidLoginUserDto() {
        LoginUserDto loginUserDto = new LoginUserDto("john_doe", "john.doe@example.com", "Password1@");

        Set<ConstraintViolation<LoginUserDto>> violations = validator.validate(loginUserDto);
        assertTrue(violations.isEmpty());
    }
}