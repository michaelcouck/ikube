package ikube.index.handler;

import ikube.model.IndexContext;
import ikube.model.Indexable;

public interface IHandler<T extends Indexable<?>> {

	public void handle(IndexContext indexContext, T indexable) throws Exception;

}
