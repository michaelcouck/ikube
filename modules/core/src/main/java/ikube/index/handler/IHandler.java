package ikube.index.handler;

import ikube.model.IndexContext;
import ikube.model.Indexable;

import java.util.List;

public interface IHandler<T extends Indexable<?>> {

	public List<Thread> handle(IndexContext indexContext, T indexable) throws Exception;

}
