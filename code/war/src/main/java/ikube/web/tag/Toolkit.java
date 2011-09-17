package ikube.web.tag;

import java.util.ArrayList;
import java.util.Collection;

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

}