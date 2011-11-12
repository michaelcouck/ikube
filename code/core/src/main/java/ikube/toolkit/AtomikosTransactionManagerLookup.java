package ikube.toolkit;

import javax.transaction.TransactionManager;

// import org.infinispan.transaction.lookup.GenericTransactionManagerLookup;

@Deprecated
public class AtomikosTransactionManagerLookup /* extends GenericTransactionManagerLookup */ {

	private static TransactionManager	transactionManager;

	public TransactionManager getTransactionManager() {
		// return ApplicationContextManager.getBean(TransactionManager.class);
		// return super.getTransactionManager();
		if (transactionManager != null) {
			// return AtomikosTransactionManagerLookup.transactionManager;
		}
		// return super.getTransactionManager();
		return null;
	}

	public static void setTransactionManager(TransactionManager transactionManager) {
		AtomikosTransactionManagerLookup.transactionManager = transactionManager;
	}

}
