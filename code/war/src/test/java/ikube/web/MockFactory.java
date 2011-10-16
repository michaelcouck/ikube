package ikube.web;

import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

public class MockFactory {

	private static Map<Class<?>, Object>	MOCKS	= new HashMap<Class<?>, Object>();

	public static Object getMock(Class<?> klass) {
		if (MOCKS.get(klass) == null) {
			Object mock = mock(klass);
			MOCKS.put(klass, mock);
		}
		return MOCKS.get(klass);
	}

	public static void removeMock(Class<?> klass) {
		MOCKS.remove(klass);
	}

}
