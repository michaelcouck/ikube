package ikube.toolkit;

import org.osjava.sj.memory.MemoryContext;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import java.io.Serializable;
import java.util.Hashtable;

/**
 * This class overrides the memory context from SimpleJndi to correct
 * the lookup method that should throw a name not found exception but does
 * not.
 *
 * @author Michael Couck
 * @author Bruno Barin
 * @version 01.00
 * @since 06.05.10
 */
@SuppressWarnings({"unchecked", "serial"})
public class InitialContext extends MemoryContext implements Serializable {

    /**
     * Constructor.
     */
    public InitialContext(final Hashtable<?, ?> env) {
        super(env);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object lookup(final String key) throws NamingException {
        Object object = super.lookup(key);
        // This is where Simple Jndi does not throw an exception
        // as is specified in the Java API for Context
        if (object == null) {
            throw new NameNotFoundException();
        }
        return object;
    }

}
