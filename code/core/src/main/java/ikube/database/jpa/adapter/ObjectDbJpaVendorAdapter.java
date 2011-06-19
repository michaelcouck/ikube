package ikube.database.jpa.adapter;

import javax.persistence.spi.PersistenceProvider;

import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;

import com.objectdb.jpa.Provider;

public class ObjectDbJpaVendorAdapter extends AbstractJpaVendorAdapter {

	@Override
	public PersistenceProvider getPersistenceProvider() {
		return new Provider();
	}

}
