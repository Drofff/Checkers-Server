package com.drofff.checkers.server.utils;

import com.drofff.checkers.server.exception.ValidationException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Map;
import java.util.Set;

import static com.drofff.checkers.server.utils.MapUtils.isNotEmpty;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toMap;

public class ValidationUtils {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory()
            .getValidator();

    private ValidationUtils() {}

    public static <T> void validate(T obj) {
        validateNotNull(obj);
        Set<ConstraintViolation<T>> violationSet = VALIDATOR.validate(obj);
        Map<String, String> fieldErrorsMap = toFieldErrorsMap(violationSet);
        if(isNotEmpty(fieldErrorsMap)) {
            throw new ValidationException(fieldErrorsMap);
        }
    }

    private static void validateNotNull(Object obj) {
        validateNotNull(obj, "Object provided should not be null");
    }

    private static <T> Map<String, String> toFieldErrorsMap(Set<ConstraintViolation<T>> violationSet) {
        return violationSet.stream()
                .collect(toMap(violation -> violation.getPropertyPath().toString(), ConstraintViolation::getMessage));
    }

    public static void validateNotNull(Object obj, String errorMessage) {
        if(isNull(obj)) {
            throw new ValidationException(errorMessage);
        }
    }

}