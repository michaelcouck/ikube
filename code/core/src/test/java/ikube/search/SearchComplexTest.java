package ikube.search;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.action.index.analyzer.StemmingAnalyzer;
import ikube.toolkit.FileUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static ikube.search.Search.TypeField.*;
import static org.junit.Assert.assertEquals;

/**
 * NOTE to self: Lucene does not like underscores in the field names!!!
 *
 * @author Michael Couck
 * @version 01.00
 * @since 20.02.2012
 */
public class SearchComplexTest extends AbstractTest {

    private SearchComplex searchComplex;

    @Before
    public void before() throws Exception {
        File file = FileUtilities.findFileRecursively(new File("."), "index-data.csv");
        List<String> lines = FileUtils.readLines(file);
        String[] columns = StringUtils.split(lines.get(0), ';');
        List<String[]> data = new ArrayList<>();

        for (final String line : lines) {
            String[] values = StringUtils.split(line, ';');
            data.add(values);
        }

        String[][] strings = data.toArray(new String[data.size()][]);
        strings = Arrays.copyOfRange(strings, 1, strings.length);
        searchComplex = createIndexRamAndSearch(SearchComplex.class, new StemmingAnalyzer(), columns, strings);
        searchComplex.setFirstResult(0);
        searchComplex.setFragment(true);
        searchComplex.setMaxResults(10);
    }

    @After
    public void after() throws Exception {
        searchComplex.searcher.getIndexReader().close();
    }

    @Test
    public void singleField() throws Exception {
        searchComplex.setSearchFields("name");
        searchComplex.setSearchStrings("Michael Couck");
        // searchComplex.setSortFields("name");
        searchComplex.setTypeFields(Search.TypeField.STRING.name());
        searchComplex.setOccurrenceFields(IConstants.SHOULD);

        ArrayList<HashMap<String, String>> results = searchComplex.execute();
        String fragment = results.get(0).get(IConstants.FRAGMENT);
        assertEquals("This is the highlighted hit : ", "cv   <B>Michael</B> <B>Couck</B>", fragment);
    }

    @Test
    public void numericQuery() throws Exception {
        searchComplex.setSearchFields("annee");
        searchComplex.setSearchStrings("2001.0");
        searchComplex.setTypeFields(NUMERIC.fieldType());
        searchComplex.setOccurrenceFields(IConstants.SHOULD);
        // searchComplex.setSortFields("annee");

        ArrayList<HashMap<String, String>> results = searchComplex.execute();
        assertEquals("There must be 1 result and the statistics : ", 2, results.size());

        searchComplex.setSearchStrings("2001", "6");
        searchComplex.setSearchFields("annee", "column-two");
        searchComplex.setTypeFields(NUMERIC.fieldType(), NUMERIC.fieldType());
        searchComplex.setOccurrenceFields(IConstants.SHOULD, IConstants.SHOULD);
        searchComplex.setSortFields("annee");

        results = searchComplex.execute();
        assertEquals("There must be 2 result and the statistics : ", 3, results.size());
    }

    @Test
    public void rangeQuery() throws Exception {
        searchComplex.setSearchFields("annee");
        searchComplex.setSearchStrings("2001-2003");
        searchComplex.setTypeFields(RANGE.fieldType());
        searchComplex.setOccurrenceFields(IConstants.SHOULD);
        searchComplex.setSortFields("annee");
        ArrayList<HashMap<String, String>> results = searchComplex.execute();
        assertEquals("There must be 3 results and the statistics : ", 4, results.size());

        searchComplex.setSearchFields("annee", "annee");
        searchComplex.setSearchStrings("2001-2003", "2005-2006");
        searchComplex.setTypeFields(RANGE.fieldType(), RANGE.fieldType());
        searchComplex.setOccurrenceFields(IConstants.SHOULD, IConstants.SHOULD);
        searchComplex.setSortFields("annee");
        results = searchComplex.execute();
        assertEquals("There must be 5 results and the statistics : ", 6, results.size());

        searchComplex.setSearchFields("column-two");
        searchComplex.setSearchStrings("5-7");
        searchComplex.setTypeFields(RANGE.fieldType());
        searchComplex.setOccurrenceFields(IConstants.SHOULD);
        searchComplex.setSortFields("column-two");
        results = searchComplex.execute();
        assertEquals("There must be 3 results and the statistics : ", 4, results.size());
    }

    @Test
    public void complexQuery() throws Exception {
        searchComplex.setSearchFields("name", "annee", "column-two");
        searchComplex.setSearchStrings("cie interc", "2002", "7-9");
        searchComplex.setTypeFields(STRING.fieldType(), NUMERIC.fieldType(), RANGE.fieldType());
        searchComplex.setOccurrenceFields(IConstants.SHOULD, IConstants.SHOULD, IConstants.SHOULD);
        searchComplex.setSortFields("column-two");

        ArrayList<HashMap<String, String>> results = searchComplex.execute();
        assertEquals("There must be 6 results and the statistics : ", 7, results.size());
    }

}
