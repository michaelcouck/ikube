package ikube.web.tag;

import ikube.toolkit.DatabaseUtilities;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.ReflectionUtils;

/**
 * A set of functions that can be used in Jsp pages, like concatenation of strings and getting the size of collections.
 * 
 * @author Michael Couck
 * @since 12.12.11
 * @version 01.00
 */
public class Toolkit {

	/**
	 * Returns the size of a collection to the pate.
	 * 
	 * @param collection the collection to get the size for
	 * @return the size of the collection
	 */
	public static int size(Collection<?> collection) {
		if (collection == null) {
			return 0;
		}
		return collection.size();
	}

	/**
	 * 
	 * 
	 * @param one
	 * @param two
	 * @return
	 */
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

	public static String subString(final String string, final int startPosition, final int maxLength) {
		if (StringUtils.isEmpty(string)) {
			return string;
		}
		String dots = "...";
		if (string.length() <= startPosition) {
			return dots;
		}
		if (string.length() <= startPosition + maxLength) {
			return string.substring(startPosition, string.length()) + dots;
		}
		return string.substring(startPosition, startPosition + maxLength) + dots;
	}

}