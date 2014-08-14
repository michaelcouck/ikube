package ikube.action.index.handler;

import java.util.List;

/**
 * This interface is for implementations that provide resources to the processing chains. Generally implementations
 * will access resources, in various ways, like from the file system, and make those resources available to callers of the
 * {@link IResourceProvider#getResource()} method. The resource provider should sleep the caller if there are resources
 * to be had, but have not yet arrived as yet. Please read the method descriptions for more information.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 02-06-2013
 */
public interface IResourceProvider<T> {

    /**
     * This method returns the parametrized type to the caller. Each provider will provide different types of resources,
     * like email messages, database resources(result sets) or internet pages. If there are sill threads accessing the
     * internet pages, but there are no resources on the stack, this method should sleep the caller until there are once again
     * resources on the stack to return to the client.
     *
     * @return the resource provided by the class
     */
    T getResource();

    /**
     * This method may be called by the framework, and offers an opportunity for implementers to populate the stack
     * of resources that they provide. This method could be synchronized, or alternatively the collection of the provider
     * could be synchronized internally.
     *
     * @param resources the resources to add to the stack of the provider
     */
    void setResources(final List<T> resources);

}
