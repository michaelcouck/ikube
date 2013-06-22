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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class InternetResourceProvider implements IResourceProvider<Url> {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

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
	public synchronized Url getResource() {
		try {
			if (urls.isEmpty()) {
				// We'll wait a few seconds to see if any other thread will add some urls to the stack
				ThreadUtilities.sleep(15000);
				if (urls.isEmpty()) {
					return null;
				} else {
					return getResource();
				}
			}
			return urls.pop();
		} finally {
			notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void setResources(final List<Url> resources) {
		try {
			if (resources == null) {
				return;
			}
			for (final Url url : resources) {
				Long hash = HashUtilities.hash(url.getUrl());
				if (!done.contains(hash)) {
					logger.info("Adding url : " + url + ", " + hash);
					urls.push(url);
					done.add(hash);
				}
			}
		} finally {
			notifyAll();
		}
	}

}