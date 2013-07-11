package ikube.action.index.handler.internet;

import ikube.action.index.handler.IResourceProvider;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.HashUtilities;
import ikube.toolkit.ThreadUtilities;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

class InternetResourceProvider implements IResourceProvider<Url> {

	private Stack<Url> urls;
	private Set<Long> done;

	InternetResourceProvider(final IndexableInternet indexableInternet) {
		urls = new Stack<Url>();
		done = new TreeSet<Long>();
		Url url = new Url();
		url.setUrl(indexableInternet.getUrl());
		url.setName(indexableInternet.getName());
		setResources(Arrays.asList(url));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Url getResource() {
		if (urls.isEmpty()) {
			// We'll wait a few seconds to see if any other thread will add some urls to the stack
			ThreadUtilities.sleep(600000);
			if (urls.isEmpty()) {
				return null;
			} else {
				return getResource();
			}
		}
		return urls.pop();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setResources(final List<Url> resources) {
		if (resources == null) {
			return;
		}
		for (final Url url : resources) {
			Long hash = HashUtilities.hash(url.getUrl());
			if (!done.contains(hash) && urls.size() < 10000000) {
				urls.push(url);
				done.add(hash);
			}
		}
	}

}