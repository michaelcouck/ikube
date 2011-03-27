package ikube.toolkit;

import java.util.Hashtable;

import javax.naming.Context;

/**
 * This class overrides the memory context from SimpleJndi to correct the lookup method that should throw a name not found exception but
 * does not.
 *
 * @author Michael Couck
 * @author Bruno Barin
 * @since 06.05.10
 * @version 01.00
 */
public class InitialContextFactory implements javax.naming.spi.InitialContextFactory {

	private transient InitialContext initialContext;

	/**
	 * {@inheritDoc}
	 */
	public Context getInitialContext(final Hashtable<?, ?> env) {
		if (initialContext == null) {
			initialContext = new InitialContext(env);
		}
		return initialContext;
	}

}
