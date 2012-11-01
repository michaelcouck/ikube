package ikube.service;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.index.IndexManager;
import ikube.index.analyzer.NgramAnalyzer;
import ikube.model.Search;
import ikube.search.SearchSingle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class will get all the existing searches from the database and index them. Then it will execute n-gram queries on the index (in
 * memory) returning the best set of matches.
 * 
 * @author Michael Couck
 * @since 29.10.12
 * @version 01.00
 */
public class AutoComplete implements IAutoComplete {

	private static final Logger LOGGER = LoggerFactory.getLogger(AutoComplete.class);

	private Analyzer analyzer;

	@Autowired
	private IDataBase dataBase;

	private Directory directory;
	private IndexSearcher indexSearcher;

	public void initialize() throws CorruptIndexException, LockObtainFailedException, IOException {
		int increment = 1000;
		int startIndex = 0;
		int endIndex = increment;
		analyzer = new NgramAnalyzer();

		List<Search> searches = dataBase.find(Search.class, startIndex, endIndex);

		directory = new RAMDirectory();
		// FSDirectory.open(FileUtilities.getFile("./autocomplete", Boolean.TRUE));
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(IConstants.VERSION, analyzer);
		indexWriterConfig.setMaxBufferedDocs(100);
		indexWriterConfig.setRAMBufferSizeMB(256);
		indexWriterConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
		IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
		do {
			for (final Search search : searches) {
				Document document = new Document();
				IndexManager.addStringField(IConstants.CONTENT, search.getSearchStrings(), document, Store.YES, Index.ANALYZED,
						TermVector.YES);
				LOGGER.info("Document : " + document);
				indexWriter.addDocument(document);
			}
			startIndex = endIndex;
			endIndex += increment;
			searches = dataBase.find(Search.class, startIndex, endIndex);
		} while (searches.size() > 0);
		IndexManager.closeIndexWriter(indexWriter);
		indexSearcher = new IndexSearcher(IndexReader.open(directory));
	}

	@Override
	public String[] suggestions(String searchString) {
		SearchSingle searchSingle = new SearchSingle(indexSearcher, analyzer);
		searchSingle.setFirstResult(0);
		searchSingle.setMaxResults(10);
		searchSingle.setFragment(Boolean.TRUE);
		searchSingle.setSearchField(IConstants.CONTENT);
		searchSingle.setSearchString(searchString);
		ArrayList<HashMap<String, String>> results = searchSingle.execute();
		LOGGER.info("Results : " + results);
		results.remove(results.size() - 1);
		int index = 0;
		String[] suggestions = new String[results.size()];
		for (final HashMap<String, String> result : results) {
			suggestions[index++] = result.get(IConstants.CONTENT);
		}
		return suggestions;
	}

}