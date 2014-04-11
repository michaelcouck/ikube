package ikube.web.tag;

import ikube.database.DatabaseUtilities;
import ikube.toolkit.VersionUtilities;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * A set of functions that can be used in Jsp pages, like concatenation of strings and getting the size of collections.
 * 
 * @author Michael Couck
 * @since 12-12-2011
 * @version 01.00
 */
public class Toolkit {

	private static final Logger LOGGER = LoggerFactory.getLogger(Toolkit.class);

	private static String VERSION;
	private static String TIMESTAMP;

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
	 * This method removes the target collection from the source.
	 * 
	 * @param one the source collection to have purged by the target
	 * @param two the target collection who's entries are to be removesd from the source
	 * @return the purged collection sans the elements in the target collection
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
		int endPosition = startPosition + maxLength;
		if (endPosition <= string.length()) {
			return string.substring(startPosition, endPosition) + dots;
		}
		return string.substring(startPosition, string.length());
	}

	/**
	 * This method will build a query string using the parameters in the map in the signature. This avoids scripting in the Jsp or very long urls built
	 * parameter by parameter.
	 * 
	 * @param parameterMap the map of parameters to use in the query string
	 * @param parameterNamesReplacements the names of the parameters to be replaced in the original map
	 * @param parameterValuesReplacements the values of the parameters to be replaced in the original map
	 * @return the string query for he parameters and replacements, would be something like '?paramOne=paramValueOne&paramTwo=paramValueTwo&...'
	 */
	public static String queryString(final Map<Object, Object> parameterMap, final List<Object> parameterNamesReplacements,
			final List<Object> parameterValuesReplacements) {
		StringBuilder stringBuilder = new StringBuilder("?");
		for (Map.Entry<Object, Object> entry : parameterMap.entrySet()) {
			Object parameterName = entry.getKey();
			String[] parameterValues = (String[]) entry.getValue();
			int indexOfParameterName = parameterNamesReplacements.indexOf(parameterName);
			if (indexOfParameterName > -1) {
				parameterValues = new String[] { parameterValuesReplacements.get(indexOfParameterName).toString() };
			}
			if (parameterValues == null || parameterValues.length == 0) {
				continue;
			}
			stringBuilder.append(parameterName);
			stringBuilder.append("=");
			stringBuilder.append(parameterValues[0]);
			stringBuilder.append("&");
		}
		return stringBuilder.toString();
	}

	/**
	 * This method will change an array to a list that is 'printer' friendly, or just return the object in a list if it is not an array.
	 * 
	 * @param object the object to convert from an array into a list
	 * @return the list of objects in the array
	 */
	public static List<Object> asList(Object object) {
		if (object.getClass().isArray()) {
			return Arrays.asList((Object[]) object);
		}
		return Arrays.asList(object);
	}

	public static String documentIcon(Object path, String icons, String def) {
		try {
			String extension = FilenameUtils.getExtension(path.toString());
			if (StringUtils.isEmpty(extension)) {
				return def;
			}
			StringTokenizer stringTokenizer = new StringTokenizer(icons, "|,;:", false);
			while (stringTokenizer.hasMoreTokens()) {
				String icon = stringTokenizer.nextToken().trim();
				if (icon.startsWith(extension)) {
					return icon;
				}
			}
		} catch (Exception e) {
			LOGGER.error("Icon access error : " + path + ", " + icons + ", " + def, e);
		}
		return def;
	}

	public static String version() {
		if (VERSION == null) {
			VersionUtilities.readPomProperties();
			VERSION = VersionUtilities.version();
		}
		return VERSION;
	}

	public static String timestamp() {
		if (TIMESTAMP == null) {
			VersionUtilities.readPomProperties();
			TIMESTAMP = VersionUtilities.timestamp();
		}
		return TIMESTAMP;
	}

}