package ikube.index.handler.internet.crawler;

import ikube.model.Url;

import java.util.Collection;

/**
 * This is the base interface for handlers and meant to be implemented to add functionality and custom handlers for specific projects. The
 * handle method takes a {@link Page} and the logic that processes that page should be included in the implementing class. Because typical
 * web data is hierarchical the handlers are composite. This matches closely natural structure of the data. As such each handler can have
 * children.
 * 
 * In the case of a page handler, the handler will get the page in the handle method, then look through the page and retrieving the links.
 * It will then go and get that page from the net and pass it to it's children. In this way the crawl can continue.
 * 
 * Handlers are typically defined in the Spring configuration for each job. If a handler has it's self as a child then the crawl could
 * potentially never end as the handler recursively calls it's self. However if the 'crawlOnce' parameter is set to true then this
 * configuration where a handler has it's self as a child will crawl the entire site once.
 * 
 * @author Michael Couck
 * @since 25.09.10
 * @version 01.00
 */
public interface IHandler<E extends Url> {
	
	/**
	 * Handles the page. This logic is dependent on the implementor.
	 * 
	 * @param url
	 *            the page to handle
	 */
	void handle(E url);

	/**
	 * This is a convenience method to call the children to handle the page from this handler.
	 * 
	 * @param page
	 *            the page for the children to handle one by one
	 */
	void handleChildren(E url);

	/** Setters and getters for the properties. */

	void setChildren(Collection<IHandler<E>> children);

	Collection<IHandler<E>> getChildren();

}
