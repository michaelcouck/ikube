package ikube.web.tag;

import ikube.toolkit.DatabaseUtilities;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

import org.springframework.util.ReflectionUtils;

public class Toolkit {

	public static int size(Collection<?> collection) {
		if (collection == null) {
			return 0;
		}
		return collection.size();
	}

	public static Collection<?> remove(Collection<?> one, Collection<?> two) {
		if (one != null && two != null) {
			one.removeAll(two);
		}
		return one;
	}

	public static boolean contains(Collection<?> coll, Object o) {
		if (coll == null || o == null) {
			return Boolean.FALSE;
		}
		return coll.contains(o);
	}

	public static String className(Object o) {
		if (o == null) {
			return null;
		}
		return o.getClass().getSimpleName();
	}

	public static Collection<?> add(Collection<Object> one, Collection<Object> two) {
		if (one == null || two == null) {
			if (one == null) {
				return two;
			}
			if (two == null) {
				return one;
			}
			return new ArrayList<Object>();
		}
		one.addAll(two);
		return one;
	}

	public static Collection<Object> clone(Collection<Object> collection) {
		if (collection == null) {
			return collection;
		}
		Collection<Object> cloned = new ArrayList<Object>();
		cloned.addAll(collection);
		return cloned;
	}

	public static String concatenate(String one, String two) {
		return new StringBuilder(one).append(two).toString();
	}
	
	public static Object fieldValue(final String fieldName, final Object object) {
		if (object == null) {
			return null;
		}
		Field field = ReflectionUtils.findField(object.getClass(), fieldName);
		ReflectionUtils.makeAccessible(field);
		return DatabaseUtilities.getFieldValue(field, object);
	}

}