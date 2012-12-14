package ikube.database.jpa;

import ikube.ATest;
import ikube.IConstants;
import ikube.model.Url;
import ikube.model.security.User;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.junit.Before;
import org.junit.Test;

public class ADataBaseJpaTest extends ATest {

	private ADataBaseJpa dataBaseJpa;

	public ADataBaseJpaTest() {
		super(ADataBaseJpaTest.class);
	}

	@Before
	public void before() {
		final EntityManager entityManager = Persistence.createEntityManagerFactory(IConstants.PERSISTENCE_UNIT_H2).createEntityManager();
		dataBaseJpa = new ADataBaseJpa() {
			@Override
			protected EntityManager getEntityManager() {
				return entityManager;
			}
		};
	}

	@Test
	public void persist() {
		dataBaseJpa.persist(new Url());
		dataBaseJpa.persist(new User());
	}

}