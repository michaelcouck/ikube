package ikube.index.content;

import ikube.model.Indexable;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public interface IContentProvider<I extends Indexable<?>> {

	public Object getContent(I indexable);

}
