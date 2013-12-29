package ikube.action.index.handler.filesystem;

import ikube.action.index.handler.IndexableHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableDictionary;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @author Michael Couck
 * @since 01.10.11
 * @version 01.00
 */
@Deprecated
public class IndexableDictionaryHandler extends IndexableHandler<IndexableDictionary> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Future<?>> handleIndexable(final IndexContext<?> indexContext, final IndexableDictionary indexable) throws Exception {
		List<Future<?>> futures = new ArrayList<Future<?>>();
		return futures;
	}

	@Override
	protected List<?> handleResource(final IndexContext<?> indexContext, final IndexableDictionary indexableDictionary, final Object resource) {
		logger.info("Handling resource : " + resource + ", thread : " + Thread.currentThread().hashCode());
		return null;
	}

}