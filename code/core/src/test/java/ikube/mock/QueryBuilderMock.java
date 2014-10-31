package ikube.mock;

import ikube.action.index.handler.database.QueryBuilder;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import mockit.Mock;
import mockit.MockClass;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 11-02-2014
 */
@MockClass(realClass = QueryBuilder.class)
public class QueryBuilderMock {
    @Mock
    public static IndexableColumn getIdColumn(final List<Indexable> indexableColumns) {
        IndexableColumn indexableColumn = mock(IndexableColumn.class);
        when(indexableColumn.getName()).thenReturn("column-name");
        when(indexableColumn.isAnalyzed()).thenReturn(Boolean.TRUE);
        when(indexableColumn.isStored()).thenReturn(Boolean.TRUE);
        return indexableColumn;
    }

}
