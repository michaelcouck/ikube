package ikube.service;

import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.toolkit.ApplicationContextManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Column;

import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.log4j.Logger;

/**
 * @see IMonitorService
 * @author Michael Couck
 * @since 28.12.10
 * @version 01.00
 */
public class MonitorService implements IMonitorService {

	private static final Logger LOGGER = Logger.getLogger(MonitorService.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getIndexNames() {
		@SuppressWarnings("rawtypes")
		Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
		List<String> indexNames = new ArrayList<String>();
		for (IndexContext<?> indexContext : indexContexts.values()) {
			indexNames.add(indexContext.getIndexName());
		}
		return indexNames.toArray(new String[indexNames.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getIndexFieldNames(final String indexName) {
		IndexContext<?> indexContext = getIndexContext(indexName);
		if (indexContext != null) {
			Set<String> fieldNames = getFields(indexContext.getIndexables(), new TreeSet<String>());
			return fieldNames.toArray(new String[fieldNames.size()]);
		}
		return new String[0];
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	public Map<String, IndexContext> getIndexContexts() {
		return ApplicationContextManager.getBeans(IndexContext.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getFieldNames(final Class<?> indexableClass) {
		List<String> fieldNames = new ArrayList<String>();
		Field[] fields = indexableClass.getDeclaredFields();
		if (fields != null) {
			for (Field field : fields) {
				Column column = field.getAnnotation(Column.class);
				if (column == null) {
					continue;
				}
				// We only take Java types, no collections or complex types
				if (field.getType().isPrimitive()) {
					fieldNames.add(field.getName());
				} else if (field.getType().getPackage().equals(Object.class.getPackage())) { 
					fieldNames.add(field.getName());
				}
			}
		}
		Class<?> superClass = indexableClass.getSuperclass();
		if (superClass != null && !Object.class.getName().equals(superClass.getName())) {
			fieldNames.addAll(Arrays.asList(getFieldNames(superClass)));
		}
		return fieldNames.toArray(new String[fieldNames.size()]);
	}

	/**
	 * Accesses the index context by the name.
	 * 
	 * @param indexName the name of the context we are looking for
	 * @return the index context with the name or null if not found
	 */
	@SuppressWarnings("rawtypes")
	protected IndexContext<?> getIndexContext(final String indexName) {
		for (Map.Entry<String, IndexContext> mapEntry : getIndexContexts().entrySet()) {
			if (mapEntry.getValue().getIndexName().equals(indexName)) {
				return mapEntry.getValue();
			}
		}
		return null;
	}

	/**
	 * Gets all the fields for the indexable. Fields are defined by adding the {@link ikube.model.Field} annotation to the field.
	 * 
	 * @param indexables the indexables to look through and get the fields
	 * @param fieldNames set of field names to collect the fields in
	 * @return the set of field names from the indexable, and child indexables if there are any
	 */
	protected Set<String> getFields(final List<Indexable<?>> indexables, final Set<String> fieldNames) {
		if (indexables != null) {
			for (Indexable<?> indexable : indexables) {
				getFields(indexable, fieldNames);
			}
		}
		return fieldNames;
	}

	/**
	 * See {@link MonitorService#getFields(List, Set)}
	 */
	protected Set<String> getFields(final Indexable<?> indexable, final Set<String> fieldNames) {
		if (indexable == null) {
			return fieldNames;
		}
		Field[] fields = indexable.getClass().getDeclaredFields();
		for (Field field : fields) {
			ikube.model.Field annotation = field.getAnnotation(ikube.model.Field.class);
			if (annotation != null) {
				try {
					Object fieldName = FieldUtils.readDeclaredField(indexable, field.getName(), Boolean.TRUE);
					if (fieldName != null) {
						fieldNames.add(fieldName.toString());
					}
				} catch (IllegalAccessException e) {
					LOGGER.error("Illegal access with forced access?", e);
				}
			}
		}
		getFields(indexable.getChildren(), fieldNames);
		return fieldNames;
	}

}