package ikube.toolkit;

import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

/**
 * This class is responsible to put objects into a simulated jndi server.
 * 
 * @author Michael Couck
 * @author Bruno Barin
 * @since 05.03.2011
 * @version 01.00
 */
public class JndiInjector {

	private static final Logger LOGGER = Logger.getLogger(JndiInjector.class);
	/** map of objects that will be present in the JNDI */
	private final transient Map<String, Object> jndiObjects;
	/** The JNDI context */
	private static transient Context CONTEXT;

	static {
		try {
			CONTEXT = new InitialContext();
		} catch (Exception e) {
			LOGGER.error("Exception accessing the initial context, nothing will work now : ", e);
		}
	}

	/**
	 * Class constructors that receives a map containing the objects that will be present in JNDI
	 * 
	 * @param jndiObjects
	 */
	public JndiInjector(final Map<String, Object> jndiObjects) {
		this.jndiObjects = jndiObjects;
	}

	/**
	 * Put all objects inside the map into JNDI
	 * 
	 * @throws Exception
	 */
	public void initialize() throws Exception {
		for (Map.Entry<String, Object> entry : jndiObjects.entrySet()) {
			JndiInjector.bind(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Bind an object to a JNDI name
	 * 
	 * @param jndiName
	 *            The jndi-name
	 * @param object
	 *            The object to be bound into JNDI
	 * @throws NamingException
	 *             If the operation is not possible to be executed.
	 */
	public static void bind(final String jndiName, final Object object) throws NamingException {
		LOGGER.debug("Binding object : " + jndiName + ":" + object.getClass());
		CONTEXT.rebind(jndiName, object);
	}

}