package com.bookcatalog.bookcatalog.helpers.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UsernameOrEmailValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface UsernameOrEmailRequired {
    String message() default "Either username or email must be provided";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

