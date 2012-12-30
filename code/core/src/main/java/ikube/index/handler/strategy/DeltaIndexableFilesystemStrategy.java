package ikube.index.handler.strategy;

import ikube.IConstants;
import ikube.index.handler.IStrategy;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystem;
import ikube.search.Search;
import ikube.search.SearchMulti;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;

/**
 * This is the delta strategy for the file system handler. Essentially what this class should do is to check to see if the document/file
 * being processed already exists in the current index. If it does, and the time stamp and the length are the same then return a false
 * indicator, meaning that the handler should not add this document to the index.
 * 
 * @author Michael Couck
 * @since 12.12.12
 * @version 01.00
 */
@SuppressWarnings("deprecation")
public class DeltaIndexableFilesystemStrategy extends AStrategy {

	public DeltaIndexableFilesystemStrategy() {
		this(null);
	}

	public DeltaIndexableFilesystemStrategy(final IStrategy nextStrategy) {
		super(nextStrategy);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean preProcess(final Object... parameters) {
		if (parameters == null || parameters.length != 3) {
			return Boolean.TRUE;
		}
		LOGGER.error("Delta file strategy : ");
		IndexableFileSystem indexableFileSystem = (IndexableFileSystem) parameters[1];
		IndexContext<?> indexContext = (IndexContext<?>) indexableFileSystem.getParent();
		// If the searcher is null then we need to process this resource
		if (indexContext.getMultiSearcher() == null) {
			return Boolean.TRUE;
		}
		File file = (File) parameters[2];
		Search search = getSearch(indexContext, indexableFileSystem, file);
		ArrayList<HashMap<String, String>> results = search.execute();
		boolean foundFile = results.size() >= 1;
		if (foundFile) {
			LOGGER.info("Deleting index entry to replace with latest version : " + file);
			if (results.size() > 1) {
				LOGGER.warn("Found multiple files with the same attributes in the index : " + file);
			}
			IndexWriter[] indexWriters = indexContext.getIndexWriters();
			for (final IndexWriter indexWriter : indexWriters) {
				Query query;
				try {
					query = search.getQuery();
					indexWriter.deleteDocuments(query);
				} catch (ParseException e) {
					LOGGER.warn("Parse exception deleting an out of date document from the index : " + e.getMessage());
				} catch (CorruptIndexException e) {
					LOGGER.error("Index corrupt, can't really recover from this : ", e);
					throw new RuntimeException(e);
				} catch (IOException e) {
					LOGGER.error("IO exception to the index? Network failure, disk failure? No recovery possible I think : ", e);
					throw new RuntimeException(e);
				} catch (Exception e) {
					LOGGER.error("General exception deleting an out of date document : ", e);
				}
			}
		}
		boolean mustProcess = foundFile & (nextStrategy != null ? nextStrategy.preProcess(parameters) : true);
		LOGGER.info("Continuing with processing : " + mustProcess);
		return mustProcess;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean postProcess(final Object... parameters) {
		return nextStrategy != null ? nextStrategy.postProcess(parameters) : true;
	}

	@SuppressWarnings("rawtypes")
	Search getSearch(final IndexContext indexContext, final IndexableFileSystem indexableFileSystem, final File file) {
		Searcher searcher = indexContext.getMultiSearcher();
		Analyzer analyzer = indexContext.getAnalyzer() != null ? indexContext.getAnalyzer() : IConstants.ANALYZER;
		Search search = new SearchMulti(searcher, analyzer);
		search.setFirstResult(0);
		search.setMaxResults(10);
		search.setFragment(Boolean.FALSE);
		search.setSearchField(indexableFileSystem.getPathFieldName(), indexableFileSystem.getNameFieldName(),
				indexableFileSystem.getLastModifiedFieldName(), indexableFileSystem.getLengthFieldName());
		search.setSearchString(file.getAbsolutePath(), file.getName(), Long.toString(file.lastModified()), Long.toString(file.length()));
		return search;
	}

}