package ikube.toolkit.data;

import java.util.List;

import javax.persistence.EntityManager;

/**
 * This class will generate an object graph based on the entity and the type. Note that the many to many type of reference is not
 * implemented, it will cause infinite recursion. This can be implemented however it is 17:05 on a Saturday and I just don't feel like it at
 * the moment, too much heavy lifting.
 * 
 * Also bi-directional is not implemented, also infinite recursion.
 * 
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class DataGeneratorFour extends ADataGenerator {

	protected int iterations;
	protected Class<?>[] classes;

	public DataGeneratorFour(EntityManager entityManager, int iterations, Class<?>... classes) {
		super(entityManager);
		this.iterations = iterations;
		this.classes = classes;
	}

	@Override
	public void generate() throws Exception {
		begin(entityManager);
		for (int i = 0; i < iterations; i++) {
			for (Class<?> klass : classes) {
				Object entity = createInstance(klass);
				entityManager.persist(entity);
			}
		}
		commit(entityManager);
	}

	public void delete(EntityManager entityManager, Class<?>... classes) {
		for (Class<?> klass : classes) {
			try {
				begin(entityManager);
				logger.info("Deleting : " + klass.getSimpleName());
				List<?> results = entityManager.createQuery("select e from " + klass.getSimpleName() + " as e").getResultList();
				for (Object object : results) {
					entityManager.remove(object);
				}
			} catch (Exception e) {
				logger.error("Exception deleting entities : ", e);
			} finally {
				commit(entityManager);
			}
		}
	}

}