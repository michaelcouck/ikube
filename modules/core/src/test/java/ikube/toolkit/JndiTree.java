package ikube.toolkit;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

/**
 * Prints the Jndi tree.
 *
 * @author Michael Couck
 * @since 25.05.10
 * @version 01.00
 */
public class JndiTree {

	private Logger logger;
	private Context context = null;
	private int indentLevel = 0;

	public static void main(String[] args) throws Exception {
		new JndiTree().printJNDITree("");
	}

	public JndiTree() throws NamingException {
		logger = Logger.getLogger(this.getClass());
		context = new InitialContext();
	}

	public void printJNDITree(String ct) {
		try {
			printNE(context.list(ct), ct);
			System.out.println("DONE");
		} catch (NamingException e) {
			logger.warn("Naming exception", e);
		}
	}

	@SuppressWarnings("unchecked")
	private void printNE(NamingEnumeration ne, String parentctx) throws NamingException {
		while (ne.hasMoreElements()) {
			NameClassPair next = (NameClassPair) ne.nextElement();
			printEntry(next);
			increaseIndent();
			printJNDITree((parentctx.length() == 0) ? next.getName() : parentctx + "/" + next.getName());
			decreaseIndent();
		}
	}

	private void printEntry(NameClassPair next) {
		try {
			System.out.println(printIndent() + "-->" + next);
		} catch (Exception e) {
			logger.error("Exception printing the jndi tree", e);
		}
	}

	private void increaseIndent() {
		indentLevel += 4;
	}

	private void decreaseIndent() {
		indentLevel -= 4;
	}

	private String printIndent() {
		StringBuffer buf = new StringBuffer(indentLevel);
		for (int i = 0; i < indentLevel; i++) {
			buf.append(" ");
		}
		return buf.toString();
	}
}