package util;

import annotation.MaxLengthRule;
import annotation.MinLengthRule;
import annotation.NotNullRule;
import annotation.RegularRule;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FiledChecker {

    private final static String MIN_LENGTH_NOT_STRING_ERROR = "error annotation MinLengthRule for %s, must be String";
    private final static String MAX_LENGTH_NOT_STRING_ERROR = "error annotation MaxLengthRule for %s , must be String";
    private final static String REGULAR_LENGTH_NOT_STRING_ERROR = "error annotation RegularRule for %s , must be String";
    private final static String methodNameTemplate = "get%s";
    private final static String NO_ERROR = null;

    public String checkAndGetErrorInfo(Object object) {
        Class<?> clazz = object.getClass();
        Field[] fields = clazz.getDeclaredFields();
        Optional<String> errorInfo = Arrays.stream(fields).map(field -> {

            String error = checkNullable(field, object, clazz);
            if (Objects.nonNull(error)) {
                return error;
            }
            error = checkMinLength(field, object, clazz);
            if (Objects.nonNull(error)) {
                return error;
            }
            error = checkMaxLength(field, object, clazz);
            if (Objects.nonNull(error)) {
                return error;
            }
            error = checkRegular(field, object, clazz);
            if (Objects.nonNull(error)) {
                return error;
            }
            return NO_ERROR;
        }).filter(Objects::nonNull).findFirst();

        return errorInfo.orElse(NO_ERROR);
    }

    private String checkRegular(Field field, Object object, Class<?> clazz) {
        RegularRule regularRule = field.getAnnotation(RegularRule.class);
        if (Objects.isNull(regularRule)) {
            return NO_ERROR;
        }
        if (isNotStringField(field)) {
            return String.format(REGULAR_LENGTH_NOT_STRING_ERROR, field.getName());
        }
        Pattern compile = Pattern.compile(regularRule.expression());
        Object invokeValue = getValue(field, clazz, object);
        if (Objects.isNull(invokeValue)) {
            return NO_ERROR;
        }
        String invokeValueString = invokeValue.toString();
        Matcher matcher = compile.matcher(invokeValueString);
        if (!matcher.find()) {
            return regularRule.message();
        }
        return NO_ERROR;
    }

    private String checkMinLength(Field field, Object object, Class<?> clazz) {
        MinLengthRule minLengthRule = field.getAnnotation(MinLengthRule.class);
        if (Objects.isNull(minLengthRule)) {
            return NO_ERROR;
        }
        if (isNotStringField(field)) {
            return String.format(MIN_LENGTH_NOT_STRING_ERROR, field.getName());
        }
        Object invokeValue = getValue(field, clazz, object);
        if (Objects.isNull(invokeValue)) {
            return minLengthRule.message();
        }
        if (invokeValue.toString().length() < minLengthRule.minLength()) {
            return minLengthRule.message();
        }
        return NO_ERROR;
    }


    private String checkMaxLength(Field field, Object object, Class<?> clazz) {
        MaxLengthRule maxLengthRule = field.getAnnotation(MaxLengthRule.class);
        if (Objects.isNull(maxLengthRule)) {
            return NO_ERROR;
        }
        if (isNotStringField(field)) {
            return String.format(MAX_LENGTH_NOT_STRING_ERROR, field.getName());
        }
        Object invokeValue = getValue(field, clazz, object);
        if (Objects.nonNull(invokeValue)) {
            String stringValue = invokeValue.toString();
            if (stringValue.length() > maxLengthRule.maxLength()) {
                return maxLengthRule.message();
            }
        }
        return NO_ERROR;
    }

    private String checkNullable(Field field, Object object, Class<?> clazz) {
        NotNullRule notNullRule = field.getAnnotation(NotNullRule.class);
        if (Objects.nonNull(notNullRule)) {
            Object invokeValue = getValue(field, clazz, object);
            if (Objects.isNull(invokeValue)) {
                return notNullRule.message();
            }
        }
        return NO_ERROR;
    }

    private Object getValue(Field field, Class<?> clazz, Object object) {
        String fieldName = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        String methodName = String.format(methodNameTemplate, fieldName);
        try {
            Method method = clazz.getMethod(methodName);
            return method.invoke(object);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            return NO_ERROR;
        }
        return NO_ERROR;
    }

    private boolean isNotStringField(Field field) {
        return !String.class.getName().equals(field.getType().getTypeName());
    }
}
