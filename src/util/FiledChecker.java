package util;

import annotation.MaxLengthRule;
import annotation.MinLengthRule;
import annotation.NotNullRule;
import annotation.RegularRule;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FiledChecker {

    private final static String MIN_LENGTH_NOT_STRING_ERROR = "error annotation MinLengthRule for %s, must be String";
    private final static String MAX_LENGTH_NOT_STRING_ERROR = "error annotation MaxLengthRule for %s , must be String";
    private final static String REGULAR_LENGTH_NOT_STRING_ERROR = "error annotation RegularRule for %s , must be String";
    private final static String methodNameTemplate = "get%s";


    /**
     * if return null, it mean it is ok.
     *
     * @param object the object you want to check.
     * @return check and get error info
     */
    public String checkAndGetErrorInfo(Object object) {
        Class<?> clazz = object.getClass();
        Field[] fields = clazz.getDeclaredFields();
        String error;
        for (Field field : fields) {
            error = checkNullable(field, object, clazz);
            if (error != null) {
                return error;
            }
            error = checkMinLength(field, object, clazz);
            if (error != null) {
                return error;
            }
            error = checkMaxLength(field, object, clazz);
            if (error != null) {
                return error;
            }
            error = checkRegular(field, object, clazz);
            if (error != null) {
                return error;
            }
        }
        return null;
    }

    private String checkRegular(Field field, Object object, Class<?> clazz) {
        RegularRule regularRule = field.getAnnotation(RegularRule.class);
        if (regularRule == null) {
            return null;
        }
        if (isNotStringField(field)) {
            return String.format(REGULAR_LENGTH_NOT_STRING_ERROR, field.getName());
        }
        Pattern compile = Pattern.compile(regularRule.expression());
        Object invokeValue = getValue(field, clazz, object);
        if (invokeValue == null) {
            return null;
        }
        String invokeValueString = invokeValue.toString();
        Matcher matcher = compile.matcher(invokeValueString);
        if (!matcher.find()) {
            return regularRule.message();
        }
        return null;
    }

    private String checkMinLength(Field field, Object object, Class<?> clazz) {
        MinLengthRule minLengthRule = field.getAnnotation(MinLengthRule.class);
        if (minLengthRule == null) {
            return null;
        }
        if (isNotStringField(field)) {
            return String.format(MIN_LENGTH_NOT_STRING_ERROR, field.getName());
        }
        Object invokeValue = getValue(field, clazz, object);
        if (invokeValue == null) {
            return minLengthRule.message();
        }
        if (invokeValue.toString().length() < minLengthRule.minLength()) {
            return minLengthRule.message();
        }
        return null;
    }


    private String checkMaxLength(Field field, Object object, Class<?> clazz) {
        MaxLengthRule maxLengthRule = field.getAnnotation(MaxLengthRule.class);
        if (maxLengthRule == null) {
            return null;
        }
        if (isNotStringField(field)) {
            return String.format(MAX_LENGTH_NOT_STRING_ERROR, field.getName());
        }
        Object invokeValue = getValue(field, clazz, object);
        if (invokeValue != null) {
            String stringValue = invokeValue.toString();
            if (stringValue.length() > maxLengthRule.maxLength()) {
                return maxLengthRule.message();
            }
        }
        return null;
    }

    private String checkNullable(Field field, Object object, Class<?> clazz) {
        NotNullRule notNullRule = field.getAnnotation(NotNullRule.class);
        if (notNullRule != null) {
            Object invokeValue = getValue(field, clazz, object);
            if (invokeValue == null) {
                return notNullRule.message();
            }
        }
        return null;
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
            return null;
        }
        return null;
    }

    private boolean isNotStringField(Field field) {
        return !String.class.getName().equals(field.getType().getTypeName());
    }
}
