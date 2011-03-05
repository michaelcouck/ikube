package ikube.toolkit;

import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * This class is responsible to put objects into a simulated jndi server.
 * 
 * @author Michael Couck
 * @author Bruno Barin
 * @since 05.03.2011
 * @version 01.00
 */
public class JndiInjector {

	/** map of objects that will be present in the JNDI */
	private Map<String, Object> jndiObjects;
	/** The JNDI context */
	private Context context;

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
		context = new InitialContext();
		for (Map.Entry<String, Object> entry : jndiObjects.entrySet()) {
			context.bind(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Bind an object to a JNDI name
	 * 
	 * @param jndiName
	 *            The jndi-name
	 * @param obj
	 *            The object to be bound into JNDI
	 * @throws NamingException
	 *             If the operation is not possible to be executed.
	 */
	public void bind(final String jndiName, final Object obj) throws NamingException {
		context.bind(jndiName, obj);
	}

}
