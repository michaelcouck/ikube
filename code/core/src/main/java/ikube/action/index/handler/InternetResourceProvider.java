package ikube.action.index.handler;

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

	Stack<Url> urls = new Stack<Url>();
	Set<Long> done = new TreeSet<Long>();

	InternetResourceProvider(final IndexableInternet indexableInternet) {
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
				ThreadUtilities.sleep(60000);
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
				Long hash = HashUtilities.hash(url.toString());
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