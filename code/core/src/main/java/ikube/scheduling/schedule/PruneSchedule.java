package ikube.scheduling.schedule;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.model.Action;
import ikube.scheduling.Schedule;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * This schedule will remove objects from the database that are not used to keep the volume of data restricted.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 22-07-2012
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public class PruneSchedule extends Schedule {

	@Autowired
	private IDataBase dataBase;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void run() {
		String[] fieldsToSortOn = new String[] { IConstants.ID };
		Boolean[] directionOfSort = new Boolean[] { true };
		delete(dataBase, ikube.model.Action.class, fieldsToSortOn, directionOfSort, IConstants.MAX_ACTIONS);
		delete(dataBase, ikube.model.Snapshot.class, fieldsToSortOn, directionOfSort, IConstants.MAX_SNAPSHOTS);
		delete(dataBase, ikube.model.Server.class, fieldsToSortOn, directionOfSort, IConstants.MAX_SERVERS);
        delete(dataBase, ikube.model.Rule.class, fieldsToSortOn, directionOfSort, IConstants.MAX_RULES);
	}

	@SuppressWarnings("unchecked")
	protected void delete(final IDataBase dataBase, final Class<?> klass, final String[] fieldsToSortOn, final Boolean[] directionOfSort, final long toRemain) {
		int batchSize = 1000;
		int count = dataBase.count(klass).intValue();
		while (count > toRemain) {
			logger.info("Count : " + count + ", to remain : " + toRemain + ", batch size : " + batchSize);
			List<?> entities = dataBase.find(klass, fieldsToSortOn, directionOfSort, 0, batchSize);
			for (final Object entity : entities) {
				if (ikube.model.Action.class.isAssignableFrom(entity.getClass())) {
					// We only delete the actions that are complete
					Action action = (Action) entity;
					if (action.getEndTime() != null) {
						dataBase.remove(entity);
					} else {
						logger.info("Not removing action : " +
						  action.getIndexName() + ", " +
						  action.getIndexableName() + ", " +
						  action.getServer());
					}
				} else {
					dataBase.remove(entity);
				}
			}
			count = dataBase.count(klass).intValue();
		}
	}

}