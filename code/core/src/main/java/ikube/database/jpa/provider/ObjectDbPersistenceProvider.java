package ikube.database.jpa.provider;

import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.ProviderUtil;

public class ObjectDbPersistenceProvider implements PersistenceProvider {

	@Override
	@SuppressWarnings("rawtypes")
	public EntityManagerFactory createEntityManagerFactory(String persistenceUnitName, Map properties) {
		return Persistence.createEntityManagerFactory(persistenceUnitName, properties);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo info, Map properties) {
		return createEntityManagerFactory(info.getPersistenceUnitName(), properties);
	}

	@Override
	public ProviderUtil getProviderUtil() {
		return null;
	}

}
