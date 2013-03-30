package ikube.action.index.content;

import java.io.OutputStream;
import java.sql.Types;

import ikube.model.Indexable;
import ikube.model.IndexableColumn;

/**
 * This is the interface to be implemented to supply functionality to access the data in various sources, like the database and the file
 * system for example.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public interface IContentProvider<I extends Indexable<?>> {

	/**
	 * This method takes the 'indexable' specific object(s) and writes the data to the output stream. In the case of a
	 * {@link IndexableColumn} the transient data(which is the object returned from the database column) is determined by the {@link Types}
	 * parameter in the {@link IndexableColumn} and used to then convert the data to a string and write to the output stream.
	 * 
	 * @param indexable
	 *            the indexable within which the data is stored in transient variables
	 * @param outputStream
	 *            the output stream to write the data to
	 */
	void getContent(I indexable, OutputStream outputStream);

}
