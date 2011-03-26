package ikube.web.tag.mock;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

public class EnumerationMock<T> implements Enumeration<T> {

	private Iterator<T> iterator;

	public EnumerationMock(Collection<T> collection) {
		if (collection != null) {
			iterator = collection.iterator();
		}
	}

	@Override
	public boolean hasMoreElements() {
		return iterator != null ? iterator.hasNext() : false;
	}

	@Override
	public T nextElement() {
		return iterator != null ? iterator.next() : null;
	}

}
