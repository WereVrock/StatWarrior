package balance;

public final class ReflectionUtil {

    public static Object castValue(Class<?> type, Object value) {
        if (type == int.class) {
            return ((Number) value).intValue();
        } else if (type == float.class) {
            return ((Number) value).floatValue();
        } else if (type == double.class) {
            return ((Number) value).doubleValue();
        } else if (type == boolean.class) {
            return (Boolean) value;
        } else if (type == String.class) {
            return value.toString();
        }
        throw new IllegalArgumentException("Unsupported field type: " + type.getName());
    }
}