package ikube.index.handler.internet.crawler;

import ikube.model.IndexContext;
import ikube.model.IndexableInternet;
import ikube.model.Url;

import java.util.Collection;

import org.apache.log4j.Logger;

/**
 * This abstract class is meant for clients to implement. It contains default methods (getters and setters, and handle children)
 * 
 * @author Michael Couck
 * @since 11.10.10
 * @version 01.00
 */
public abstract class Handler<E extends Url> implements IHandler<Url> {

	protected static Logger LOGGER = Logger.getLogger(Handler.class);

	private IndexContext indexContext;
	private IndexableInternet indexableInternet;
	private Collection<IHandler<Url>> children;

	/**
	 * Default implementation that calls all the children with the page.
	 */
	public void handleChildren(Url url) {
		if (getChildren() == null) {
			return;
		}
		for (IHandler<Url> child : getChildren()) {
			try {
				child.handle(url);
			} catch (Exception e) {
				LOGGER.error("Exception executing the child handler : " + child, e);
			}
		}
	}

	public IndexContext getIndexContext() {
		return indexContext;
	}

	public void setIndexContext(IndexContext indexContext) {
		this.indexContext = indexContext;
	}

	public IndexableInternet getIndexableInternet() {
		return indexableInternet;
	}

	public void setIndexableInternet(IndexableInternet indexableInternet) {
		this.indexableInternet = indexableInternet;
	}

	public Collection<IHandler<Url>> getChildren() {
		return children;
	}

	public void setChildren(Collection<IHandler<Url>> children) {
		this.children = children;
	}

}
