package ikube.service;

import ikube.database.IDataBase;
import ikube.model.Attribute;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.toolkit.ApplicationContextManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Column;

import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;

/**
 * @see IMonitorService
 * @author Michael Couck
 * @since 28.12.10
 * @version 01.00
 */
public class MonitorService implements IMonitorService {

	private static final Logger LOGGER = Logger.getLogger(MonitorService.class);

	@Autowired
	private IDataBase dataBase;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getIndexNames() {
		@SuppressWarnings("rawtypes")
		Map<String, IndexContext> indexContexts = getIndexContexts();
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
		Map<String, IndexContext> indexContexts = new HashMap<String, IndexContext>();
		indexContexts.putAll(ApplicationContextManager.getBeans(IndexContext.class));
		Collection<IndexContext> dbIndexContexts = dataBase.find(IndexContext.class, 0, Integer.MAX_VALUE);
		for (IndexContext<?> dbIndexContext : dbIndexContexts) {
			indexContexts.put(dbIndexContext.getName(), dbIndexContext);
		}
		return indexContexts;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getFieldNames(final Class<?> indexableClass) {
		final List<String> fieldNames = new ArrayList<String>();
		ReflectionUtils.doWithFields(indexableClass, new ReflectionUtils.FieldCallback() {
			@Override
			public void doWith(final Field field) throws IllegalArgumentException, IllegalAccessException {
				Column column = field.getAnnotation(Column.class);
				// We only take Java types, no collections or complex types
				if (column == null || !(field.getType().isPrimitive() || field.getType().getPackage().equals(Object.class.getPackage()))) {
					return;
				}
				fieldNames.add(field.getName());
			}
		});
		return fieldNames.toArray(new String[fieldNames.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getFieldDescriptions(final Class<?> indexableClass) {
		final List<String> fieldDescriptions = new ArrayList<String>();
		ReflectionUtils.doWithFields(indexableClass, new ReflectionUtils.FieldCallback() {
			@Override
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				Attribute attribute = field.getAnnotation(Attribute.class);
				if (attribute == null || !(field.getType().isPrimitive() || field.getType().getPackage().equals(Object.class.getPackage()))) {
					return;
				}
				fieldDescriptions.add(attribute.description());
			}
		});
		return fieldDescriptions.toArray(new String[fieldDescriptions.size()]);
	}

	/**
	 * Accesses the index context by the name.
	 * 
	 * @param indexName the name of the context we are looking for
	 * @return the index context with the name or null if not found
	 */
	@SuppressWarnings("rawtypes")
	public IndexContext<?> getIndexContext(final String indexName) {
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
			Attribute annotation = field.getAnnotation(Attribute.class);
			if (annotation != null && annotation.field()) {
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