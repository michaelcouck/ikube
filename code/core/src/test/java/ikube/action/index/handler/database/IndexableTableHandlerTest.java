package ikube.action.index.handler.database;

import ikube.AbstractTest;
import ikube.action.index.content.IContentProvider;
import ikube.action.index.handler.ResourceHandler;
import ikube.mock.DatabaseUtilitiesMock;
import ikube.mock.QueryBuilderMock;
import ikube.model.Indexable;
import ikube.toolkit.DatabaseUtilities;
import mockit.Deencapsulation;
import mockit.Mockit;
import org.apache.lucene.document.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinTask;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 29-11-2010
 */
public class IndexableTableHandlerTest extends AbstractTest {

    private ResultSet resultSet;
    private ResultSetMetaData resultSetMetaData;
    private IndexableTableHandler indexableTableHandler;

    @Before
    public void before() throws Exception {
        resultSet = mock(ResultSet.class);
        resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getObject(any(String.class))).thenReturn("Hello world");
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);

        ResourceHandler resourceHandler = mock(ResourceHandler.class);
        indexableTableHandler = new IndexableTableHandler();
        Deencapsulation.setField(indexableTableHandler, "resourceHandler", resourceHandler);
        Mockit.setUpMocks(DatabaseUtilitiesMock.class);
    }

    @After
    public void after() {
        Mockit.tearDownMocks(DatabaseUtilities.class);
    }

    @Test
    public void handleIndexableForked() throws Exception {
        ForkJoinTask forkJoinTask = indexableTableHandler.handleIndexableForked(indexContext, indexableTable);
        assertNotNull(forkJoinTask);
    }

    @Test
    public void handleResource() throws Exception {
        indexableTableHandler.handleResource(indexContext, indexableTable, resultSet);
        verify(resultSet, atLeastOnce()).next();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void handleRow() throws Exception {
        Document document = new Document();
        IContentProvider contentProvider = mock(IContentProvider.class);
        indexableTableHandler.handleRow(indexableTable, resultSet, document, contentProvider);
        verify(indexableTable, atLeastOnce()).getChildren();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void handleColumn() throws Exception {
        Document document = new Document();
        IContentProvider contentProvider = mock(IContentProvider.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocationOnMock) throws Throwable {
                OutputStream outputStream = (OutputStream) invocationOnMock.getArguments()[1];
                outputStream.write("hello world".getBytes());
                return null;
            }
        }).when(contentProvider).getContent(any(Indexable.class), any(OutputStream.class));
        indexableTableHandler.handleColumn(contentProvider, indexableColumn, document);
        verify(indexableColumn, atLeastOnce()).getFieldName();
    }

    @Test
    public void setIdField() throws Exception {
        try {
            Mockit.setUpMocks(QueryBuilderMock.class);
            Document document = new Document();
            indexableTableHandler.setIdField(indexableTable, document);
            verify(indexableTable, atLeastOnce()).getName();
        } finally {
            Mockit.tearDownMocks(QueryBuilder.class);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void setColumnTypesAndData() throws Exception {
        String columnName = "columnName";
        when(indexableColumn.getName()).thenReturn(columnName);
        when(resultSetMetaData.getColumnName(anyInt())).thenReturn(columnName);
        List children = Arrays.asList(indexableColumn);
        indexableTableHandler.setColumnTypesAndData(children, resultSet);
        verify(indexableColumn, atLeastOnce()).setContent(any(Object.class));
    }


}