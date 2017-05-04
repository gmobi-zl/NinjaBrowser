package io.github.mthli.Ninja.Utils;

public class Convert {

	public static Boolean toBoolean(Object value) {
		if (value instanceof Boolean) {
			return (Boolean) value;
		} else if (value instanceof CharSequence) {
			String stringValue = value.toString();
			if ("true".equalsIgnoreCase(stringValue)) {
				return true;
			} else if ("false".equalsIgnoreCase(stringValue)) {
				return false;
			} else if ("1".equalsIgnoreCase(stringValue)) {
				return true;
			} else if ("0".equalsIgnoreCase(stringValue)) {
				return false;
			}
		} else if (value instanceof Number){
			return ((Number)value).intValue() != 0;
		}
		return null;
	}

	public static Double toDouble(Object value) {
		if (value instanceof Double) {
			return (Double) value;
		} else if (value instanceof Number) {
			return ((Number) value).doubleValue();
		} else if (value instanceof CharSequence) {
			return Double.valueOf(value.toString());
		}
		return null;
	}

	public static Integer toInteger(Object value, Integer def) {
		if (value instanceof Integer) {
			return (Integer) value;
		} else if (value instanceof Number) {
			return ((Number) value).intValue();
		} else if (value instanceof CharSequence) {
			return Integer.valueOf(value.toString());
		}
		return def;
	}

	public static Integer toInteger(Object value) {
		return toInteger(value, null);
	}
	public static Long toLong(Object value) {
		if (value instanceof Long) {
			return (Long) value;
		} else if (value instanceof Number) {
			return ((Number) value).longValue();
		} else if (value instanceof CharSequence) {
			return Long.valueOf(value.toString());
		}
		return null;
	}

	public static String toString(Object value) {
		if (value instanceof String) {
			return (String) value;
		} else if (value != null) {
			return String.valueOf(value);
		}
		return null;
	}

}
