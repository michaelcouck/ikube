package ikube.index.content;

import ikube.model.Indexable;

public interface IContentProvider<I extends Indexable<?>> {

	public Object getContent(I indexable);

}
