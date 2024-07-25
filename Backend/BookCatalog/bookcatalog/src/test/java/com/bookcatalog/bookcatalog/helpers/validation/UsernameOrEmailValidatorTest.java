package com.bookcatalog.bookcatalog.helpers.validation;

import com.bookcatalog.bookcatalog.model.dto.LoginUserDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class UsernameOrEmailValidatorTest {

    private UsernameOrEmailValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    public void setUp() {

        validator = new UsernameOrEmailValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    public void testInitialize() {

        // Arrange
        UsernameOrEmailRequired constraintAnnotation = mock(UsernameOrEmailRequired.class);

        // Act
        validator.initialize(constraintAnnotation);

        // Assert
        // No specific assertion as initialize does not alter the state for now.
        // This test ensures no exceptions are thrown during initialization.
        assertTrue(true);
    }

    @Test
    public void testIsValid_WhenUsernameIsPresent_ShouldReturnTrue() {

        // Arrange
        LoginUserDto loginUserDto = new LoginUserDto();
        loginUserDto.setUsername("testUser");

        // Act
        boolean result = validator.isValid(loginUserDto, context);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testIsValid_WhenEmailIsPresent_ShouldReturnTrue() {
        // Arrange
        LoginUserDto loginUserDto = new LoginUserDto();
        loginUserDto.setEmail("test@example.com");

        // Act
        boolean result = validator.isValid(loginUserDto, context);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testIsValid_WhenUsernameAndEmailAreBothPresent_ShouldReturnTrue() {

        // Arrange
        LoginUserDto loginUserDto = new LoginUserDto();
        loginUserDto.setUsername("testUser");
        loginUserDto.setEmail("test@example.com");

        // Act
        boolean result = validator.isValid(loginUserDto, context);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testIsValid_WhenBothUsernameAndEmailAreNull_ShouldReturnFalse() {

        // Arrange
        LoginUserDto loginUserDto = new LoginUserDto();

        // Act
        boolean result = validator.isValid(loginUserDto, context);

        // Assert
        assertFalse(result);
    }

    @Test
    public void testIsValid_WhenBothUsernameAndEmailAreEmpty_ShouldReturnFalse() {
        // Arrange
        LoginUserDto loginUserDto = new LoginUserDto();
        loginUserDto.setUsername("");
        loginUserDto.setEmail("");

        // Act
        boolean result = validator.isValid(loginUserDto, context);

        // Assert
        assertFalse(result);
    }

    @Test
    public void testIsValid_WhenUsernameIsEmptyAndEmailIsNull_ShouldReturnFalse() {
        // Arrange
        LoginUserDto loginUserDto = new LoginUserDto();
        loginUserDto.setUsername("");

        // Act
        boolean result = validator.isValid(loginUserDto, context);

        // Assert
        assertFalse(result);
    }

    @Test
    public void testIsValid_WhenUsernameIsNullAndEmailIsEmpty_ShouldReturnFalse() {
        // Arrange
        LoginUserDto loginUserDto = new LoginUserDto();
        loginUserDto.setEmail("");

        // Act
        boolean result = validator.isValid(loginUserDto, context);

        // Assert
        assertFalse(result);
    }
}