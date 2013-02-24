package ikube;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Ikube {

	private static final Logger LOGGER = LoggerFactory.getLogger(Ikube.class);

	public static void main(String[] args) {
		try {
			// First parameter is the class
			Class<?> target = Class.forName(args[0]);
			Method method = target.getDeclaredMethod("main", String[].class);
			String[] newArgs = new String[args.length - 1];
			System.arraycopy(args, 1, newArgs, 0, newArgs.length);
			method.invoke(target, new Object[] { newArgs });
		} catch (Exception e) {
			LOGGER.error("Usage is: [class-to-execute] <parameters...>", e);
		}
	}

}
