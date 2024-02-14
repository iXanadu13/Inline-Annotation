package com.github.ixanadu13.annotation.processor.shade.lombok.configuration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ConfigurationDataType {
    private static final Map<Class<?>, ConfigurationValueParser> SIMPLE_TYPES;
    static {
        Map<Class<?>, ConfigurationValueParser> map = new HashMap<Class<?>, ConfigurationValueParser>();
        map.put(String.class, new ConfigurationValueParser() {
            @Override public Object parse(String value) {
                return value;
            }

            @Override public String description() {
                return "string";
            }

            @Override public String exampleValue() {
                return "<text>";
            }
        });
        map.put(Integer.class, new ConfigurationValueParser() {
            @Override public Object parse(String value) {
                return Integer.parseInt(value);
            }

            @Override public String description() {
                return "int";
            }

            @Override public String exampleValue() {
                return "<int>";
            }
        });
        map.put(Long.class, new ConfigurationValueParser() {
            @Override public Object parse(String value) {
                return Long.parseLong(value);
            }

            @Override public String description() {
                return "long";
            }

            @Override public String exampleValue() {
                return "<long>";
            }
        });
        map.put(Double.class, new ConfigurationValueParser() {
            @Override public Object parse(String value) {
                return Double.parseDouble(value);
            }

            @Override public String description() {
                return "double";
            }

            @Override public String exampleValue() {
                return "<double>";
            }
        });
        map.put(Boolean.class, new ConfigurationValueParser() {
            @Override public Object parse(String value) {
                return Boolean.parseBoolean(value);
            }

            @Override public String description() {
                return "boolean";
            }

            @Override public String exampleValue() {
                return "[false | true]";
            }
        });
        SIMPLE_TYPES = map;
    }

    private static ConfigurationValueParser enumParser(Type enumType) {
        final Class<?> type = (Class<?>) enumType;
        @SuppressWarnings("rawtypes") final Class rawType = type;

        return new ConfigurationValueParser() {
            @SuppressWarnings("unchecked")
            @Override public Object parse(String value) {
                try {
                    return Enum.valueOf(rawType, value);
                } catch (Exception e) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < value.length(); i++) {
                        char c = value.charAt(i);
                        if (Character.isUpperCase(c) && i > 0) sb.append("_");
                        sb.append(Character.toUpperCase(c));
                    }
                    return Enum.valueOf(rawType, sb.toString());
                }
            }

            @Override public String description() {
                return "enum (" + type.getName() + ")";
            }

            @Override public String exampleValue() {
                ExampleValueString evs = type.getAnnotation(ExampleValueString.class);
                if (evs != null) return evs.value();
                return Arrays.toString(type.getEnumConstants()).replace(",", " |");
            }
        };
    }

    private static ConfigurationValueParser valueTypeParser(Type argumentType) {
        final Class<?> type = (Class<?>) argumentType;
        final Method valueOfMethod = getMethod(type, "valueOf", String.class);
        final Method descriptionMethod = getMethod(type, "description");
        final Method exampleValueMethod = getMethod(type, "exampleValue");
        return new ConfigurationValueParser() {
            @Override public Object parse(String value) {
                return invokeStaticMethod(valueOfMethod, value);
            }

            @Override public String description() {
                return invokeStaticMethod(descriptionMethod);
            }

            @Override public String exampleValue() {
                return invokeStaticMethod(exampleValueMethod);
            }

            @SuppressWarnings("unchecked")
            private <R> R invokeStaticMethod(Method method, Object... arguments) {
                try {
                    return (R) method.invoke(null, arguments);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("The method " + method.getName() + " ", e);
                } catch (InvocationTargetException e) {
                    // There shouldn't be any checked Exception, only IllegalArgumentException is expected
                    throw (RuntimeException) e.getTargetException();
                }
            }
        };
    }

    private final boolean isList;
    private final ConfigurationValueParser parser;

    public static ConfigurationDataType toDataType(Class<? extends ConfigurationKey<?>> keyClass) {
        if (keyClass.getSuperclass() != ConfigurationKey.class) {
            throw new IllegalArgumentException("No direct subclass of ConfigurationKey: " + keyClass.getName());
        }

        Type type = keyClass.getGenericSuperclass();
        if (!(type instanceof ParameterizedType)) {
            throw new IllegalArgumentException("Missing type parameter in " + type);
        }

        ParameterizedType parameterized = (ParameterizedType) type;
        Type argumentType = parameterized.getActualTypeArguments()[0];

        boolean isList = false;
        if (argumentType instanceof ParameterizedType) {
            ParameterizedType parameterizedArgument = (ParameterizedType) argumentType;
            if (parameterizedArgument.getRawType() == List.class) {
                isList = true;
                argumentType = parameterizedArgument.getActualTypeArguments()[0];
            }
        }

        if (SIMPLE_TYPES.containsKey(argumentType)) {
            return new ConfigurationDataType(isList, SIMPLE_TYPES.get(argumentType));
        }

        if (isEnum(argumentType)) {
            return new ConfigurationDataType(isList, enumParser(argumentType));
        }

        if (isConfigurationValueType(argumentType)) {
            return new ConfigurationDataType(isList, valueTypeParser(argumentType));
        }

        throw new IllegalArgumentException("Unsupported type parameter in " + type);
    }

    private ConfigurationDataType(boolean isList, ConfigurationValueParser parser) {
        this.isList = isList;
        this.parser = parser;
    }

    public boolean isList() {
        return isList;
    }

    ConfigurationValueParser getParser() {
        return parser;
    }

    @Override
    public String toString() {
        if (isList) return "list of " + parser.description();
        return parser.description();
    }

    private static boolean isEnum(Type argumentType) {
        return argumentType instanceof Class && ((Class<?>) argumentType).isEnum();
    }

    private static boolean isConfigurationValueType(Type argumentType) {
        return argumentType instanceof Class && ConfigurationValueType.class.isAssignableFrom((Class<?>) argumentType);
    }

    private static Method getMethod(Class<?> argumentType, String name, Class<?>... parameterTypes) {
        try {
            return argumentType.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Method " + name + " with parameters " + Arrays.toString(parameterTypes) + " was not found.", e);
        } catch (SecurityException e) {
            throw new IllegalStateException("Cannot inspect methods of type " + argumentType, e);
        }
    }
}