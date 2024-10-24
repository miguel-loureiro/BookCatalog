package com.newbookcatalog.newbookcatalog.helpers.validation;

import com.newbookcatalog.newbookcatalog.model.dto.LoginUserDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UsernameOrEmailValidator implements ConstraintValidator<UsernameOrEmailRequired, LoginUserDto> {

    @Override
    public void initialize(UsernameOrEmailRequired constraintAnnotation) {

        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(LoginUserDto loginUserDto, ConstraintValidatorContext constraintValidatorContext) {

        return loginUserDto.getUsername() != null && !loginUserDto.getUsername().isEmpty() ||
                loginUserDto.getEmail() != null && !loginUserDto.getEmail().isEmpty();
    }
}
