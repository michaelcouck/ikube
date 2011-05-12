package ikube.index.handler.internet.crawler;

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
public abstract class UrlHandler<E extends Url> implements IUrlHandler<Url> {

	protected static final Logger LOGGER = Logger.getLogger(UrlHandler.class);

	private Collection<IUrlHandler<Url>> children;

	/**
	 * Default implementation that calls all the children with the page.
	 */
	public void handleChildren(final Url url) {
		if (getChildren() == null) {
			return;
		}
		for (IUrlHandler<Url> child : getChildren()) {
			try {
				child.handle(url);
			} catch (Exception e) {
				LOGGER.error("Exception executing the child handler : " + child, e);
			}
		}
	}

	public Collection<IUrlHandler<Url>> getChildren() {
		return children;
	}

	public void setChildren(final Collection<IUrlHandler<Url>> children) {
		this.children = children;
	}

}
