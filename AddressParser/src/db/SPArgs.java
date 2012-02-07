package db;

import java.util.HashMap;

public class SPArgs extends HashMap<Object, Object> {
	private static final long serialVersionUID = 1L;
	public static final int INTEGER = 1;
	public static final int DATE = 2;
	public static final int STRING = 3;

	public Object put(Object key, Object value) {
		Object obj = null;
		if (value == null) {
			obj = super.put(key, "NULLVarchar");
		} else {
			obj = super.put(key, value);
		}
		return obj;
	}

	public Object put(Object key, int value) {
		Object obj = super.put(key, Integer.toString(value));
		return obj;
	}

	public Object put(Object key, Object value, int type) {
		Object obj = null;
		if (value == null) {
			if (type == SPArgs.INTEGER) {
				obj = super.put(key, "NULLInteger");
			} else if (type == SPArgs.DATE) {
				obj = super.put(key, "NULLDate");
			} else {
				obj = super.put(key, "NULLVarchar");
			}
		} else {
			obj = super.put(key, value);
		}
		return obj;
	}
}
