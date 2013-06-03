package ikube.action.index.handler;

import java.util.List;

public interface IResourceProvider<T> {

	T getResource();

	void setResources(final List<T> resources);

}
