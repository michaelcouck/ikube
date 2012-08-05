package ikube.service;

import ikube.IConstants;
import ikube.index.IndexManager;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

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
	public String[] getIndexableNames(final String indexName) {
		IndexContext<?> indexContext = getIndexContext(indexName);
		List<Indexable<?>> indexables = indexContext.getIndexables();
		String[] indexableNames = new String[indexables.size()];
		int index = 0;
		for (Indexable<?> indexable : indexables) {
			indexableNames[index++] = indexable.getName();
		}
		return indexableNames;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getIndexContextNames() {
		@SuppressWarnings("rawtypes")
		Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
		return indexContexts.keySet().toArray(new String[indexContexts.keySet().size()]);
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
	@Override
	@SuppressWarnings("rawtypes")
	public String[] getIndexableFieldNames(final String indexableName) {
		Map<String, Indexable> indexables = ApplicationContextManager.getBeans(Indexable.class);
		Indexable<?> indexable = null;
		for (Indexable ind : indexables.values()) {
			if (ind.getName().equals(indexableName)) {
				indexable = ind;
				break;
			}
		}
		Set<String> fieldNames = getFields(indexable, new TreeSet<String>());
		return fieldNames.toArray(new String[fieldNames.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getIndexSize(final String indexName) {
		long length = 0;
		try {
			IndexContext<?> indexContext = getIndexContext(indexName);
			if (indexContext == null) {
				LOGGER.warn("No index context with name : " + indexName);
				return length;
			}
			// indexContext.getIndexDirectoryPath() + IConstants.SEP + indexContext.getIndexName()
			String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
			File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(indexDirectoryPath);
			if (latestIndexDirectory != null) {
				File[] serverIndexDirectories = latestIndexDirectory.listFiles();
				for (File serverIndexDirectory : serverIndexDirectories) {
					LOGGER.debug("Server index directory : " + serverIndexDirectory);
					File[] files = serverIndexDirectory.listFiles();
					if (files != null) {
						for (File file : files) {
							if (file.isFile()) {
								length += file.length();
							}
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception accessing the index context for : " + indexName, e);
		}
		return length;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getIndexDocuments(final String indexName) {
		int numDocs = 0;
		Directory directory = null;
		IndexReader indexReader = null;
		try {
			IndexContext<?> indexContext = getIndexContext(indexName);
			if (indexContext == null) {
				LOGGER.warn("No index context with name : " + indexName);
				return numDocs;
			}
			String indexDirectoryPath = indexContext.getIndexDirectoryPath() + IConstants.SEP + indexContext.getIndexName();
			File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(indexDirectoryPath);
			LOGGER.debug("Looking for index in : " + indexDirectoryPath + ", " + latestIndexDirectory);
			if (latestIndexDirectory != null) {
				File[] serverIndexDirectories = latestIndexDirectory.listFiles();
				for (File serverIndexDirectory : serverIndexDirectories) {
					LOGGER.debug("Server index directory : " + serverIndexDirectory);
					try {
						directory = FSDirectory.open(serverIndexDirectory);
						if (IndexReader.indexExists(directory) && !IndexWriter.isLocked(directory)) {
							indexReader = IndexReader.open(directory);
							numDocs = indexReader.numDocs();
						}
					} finally {
						closeIndexReader(indexReader);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception checking the documents in the index : " + indexName, e);
		} finally {
			closeIndexReader(indexReader);
		}
		return numDocs;
	}

	@SuppressWarnings("rawtypes")
	public Map<String, IndexContext> getIndexContexts() {
		return ApplicationContextManager.getBeans(IndexContext.class);
	}

	/**
	 * Accesses the index context by the name.
	 * 
	 * @param indexName
	 *            the name of the context we are looking for
	 * @return the index context with the name or null if not found
	 */
	protected IndexContext<?> getIndexContext(String indexName) {
		String[] indexContextNames = getIndexContextNames();
		IndexContext<?> indexContext = null;
		for (String indexContextName : indexContextNames) {
			IndexContext<?> context = ApplicationContextManager.getBean(indexContextName);
			if (context.getIndexName().equals(indexName)) {
				indexContext = context;
				break;
			}
		}
		return indexContext;
	}

	/**
	 * Closes the index reader after we have counted the document in it.
	 * 
	 * @param indexReader
	 *            the reader to close
	 */
	protected void closeIndexReader(IndexReader indexReader) {
		if (indexReader == null) {
			return;
		}
		Directory directory = indexReader.directory();
		try {
			if (directory != null) {
				directory.close();
			}
		} catch (Exception e) {
			LOGGER.error("Exception closing the directory : " + indexReader, e);
		}
		try {
			indexReader.close();
		} catch (Exception e) {
			LOGGER.error("Exception closing the reader : " + indexReader, e);
		}
	}

	/**
	 * Gets all the fields for the indexable. Fields are defined by adding the {@link ikube.model.Field} annotation to the field.
	 * 
	 * @param indexables
	 *            the indexables to look through and get the fields
	 * @param fieldNames
	 *            set of field names to collect the fields in
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