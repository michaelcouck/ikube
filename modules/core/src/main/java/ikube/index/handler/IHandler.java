package ikube.index.handler;

import ikube.model.IndexContext;
import ikube.model.Indexable;

import java.util.List;

/**
 * @author Michael Couck
 * @since 29.11.10
 * @version 01.00
 */
public interface IHandler<T extends Indexable<?>> {

	public List<Thread> handle(IndexContext indexContext, T indexable) throws Exception;

}
