package ikube.service;

import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.toolkit.ApplicationContextManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.ejb.Remote;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.log4j.Logger;

/**
 * 
 * @author Michael Couck
 * @since 28.12.10
 * @version 01.00
 */
@Remote(IMonitoringService.class)
@SOAPBinding(style = SOAPBinding.Style.RPC)
@WebService(name = IMonitoringService.NAME, targetNamespace = IMonitoringService.NAMESPACE, serviceName = IMonitoringService.SERVICE)
public class MonitoringService implements IMonitoringService {

	private static final Logger LOGGER = Logger.getLogger(MonitoringService.class);

	public String[] getIndexNames() {
		Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
		List<String> indexNames = new ArrayList<String>();
		for (IndexContext indexContext : indexContexts.values()) {
			indexNames.add(indexContext.getIndexName());
		}
		return indexNames.toArray(new String[indexNames.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getIndexContextNames() {
		Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
		return indexContexts.keySet().toArray(new String[indexContexts.keySet().size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getIndexFieldNames(final String indexName) {
		Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
		for (IndexContext indexContext : indexContexts.values()) {
			if (indexContext.getIndexName().equals(indexName)) {
				Set<String> fieldNames = getFields(indexContext.getIndexables(), new TreeSet<String>());
				return fieldNames.toArray(new String[fieldNames.size()]);
			}
		}
		return new String[0];
	}

	@Override
	public String[] getIndexableFieldNames(final String indexableName) {
		Indexable<?> indexable = ApplicationContextManager.getBean(indexableName);
		Set<String> fieldNames = getFields(indexable, new TreeSet<String>());
		return fieldNames.toArray(new String[fieldNames.size()]);
	}

	protected Set<String> getFields(final List<Indexable<?>> indexables, final Set<String> fieldNames) {
		if (indexables != null) {
			for (Indexable<?> child : indexables) {
				getFields(child, fieldNames);
				getFields(child.getChildren(), fieldNames);
			}
		}
		return fieldNames;
	}

	protected Set<String> getFields(final Indexable<?> indexable, final Set<String> fieldNames) {
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
		return fieldNames;
	}

}