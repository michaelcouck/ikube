package ikube.index.content;

import java.io.OutputStream;

import ikube.model.Indexable;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public interface IContentProvider<I extends Indexable<?>> {

	public void getContent(I indexable, OutputStream outputStream);

}
