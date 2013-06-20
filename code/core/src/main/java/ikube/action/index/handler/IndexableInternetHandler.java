package ikube.action.index.handler;

import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.HashUtilities;
import ikube.toolkit.UriUtilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveAction;

import javax.swing.text.html.HTML;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.Tag;

public class IndexableInternetHandler extends IndexableHandler<IndexableInternet> {

	class ResourceProvider implements IResourceProvider<Url> {

		Stack<Url> urls = new Stack<Url>();
		Set<Long> done = new TreeSet<Long>();

		ResourceProvider(final IndexableInternet indexableInternet) {
			Url url = new Url();
			url.setUrl(indexableInternet.getUrl());
			url.setName(indexableInternet.getName());
			setResources(Arrays.asList(url));
		}

		public synchronized Url getResource() {
			try {
				if (urls.size() == 0) {
					return null;
				}
				return urls.pop();
			} finally {
				notifyAll();
			}
		}

		@Override
		public synchronized void setResources(final List<Url> resources) {
			try {
				if (resources == null) {
					return;
				}
				for (final Object resource : resources) {
					Url url = (Url) resource;
					Long hash = HashUtilities.hash(url.getUrl());
					if (!done.contains(hash)) {
						logger.info("Adding url : " + url.getUrl() + ", " + hash + ", " + done);
						url.setUrlId(hash);
						url.setIndexed(Boolean.FALSE);
						urls.push(url);
						done.add(hash);
					}
				}
			} finally {
				notifyAll();
			}
		}

	}

	@Override
	public List<Future<?>> handleIndexable(final IndexContext<?> indexContext, final IndexableInternet indexableInternet) throws Exception {
		ResourceProvider resourceProvider = new ResourceProvider(indexableInternet);
		ForkJoinPool forkJoinPool = new ForkJoinPool(indexableInternet.getThreads());
		RecursiveAction recursiveAction = getRecursiveAction(indexContext, indexableInternet, resourceProvider);
		forkJoinPool.invoke(recursiveAction);
		return new ArrayList<Future<?>>(Arrays.asList(recursiveAction));
	}

	@Override
	protected List<?> handleResource(final IndexContext<?> indexContext, final IndexableInternet indexableInternet, final Object resource) {
		try {
			Url url = (Url) resource;
			logger.info("Handling resource : " + url.getUrl() + ", " + this);
			return extractLinksFromContent((IndexableInternet) indexableInternet, new URL(url.getUrl()).openStream());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected List<Url> extractLinksFromContent(final IndexableInternet indexableInternet, final InputStream inputStream) {
		List<Url> urls = new ArrayList<Url>();
		try {
			Reader reader = new InputStreamReader(inputStream, IConstants.ENCODING);
			Source source = new Source(reader);
			List<Tag> tags = source.getAllTags();
			String baseUrlStripped = indexableInternet.getBaseUrl();
			for (final Tag tag : tags) {
				if (tag.getName().equals(HTMLElementName.A) && StartTag.class.isAssignableFrom(tag.getClass())) {
					Attribute attribute = ((StartTag) tag).getAttributes().get(HTML.Attribute.HREF.toString());
					if (attribute == null) {
						continue;
					}
					try {
						String link = attribute.getValue();
						if (link == null) {
							continue;
						}
						if (UriUtilities.isExcluded(link.trim().toLowerCase())) {
							continue;
						}
						String resolvedLink = UriUtilities.resolve(indexableInternet.getUri(), link);
						String replacement = resolvedLink.contains("?") ? "?" : "";
						String strippedSessionLink = UriUtilities.stripJSessionId(resolvedLink, replacement);
						String strippedAnchorLink = UriUtilities.stripAnchor(strippedSessionLink, "");
						if (!UriUtilities.isInternetProtocol(strippedAnchorLink)) {
							continue;
						}
						if (!strippedAnchorLink.startsWith(baseUrlStripped)) {
							continue;
						}
						if (indexableInternet.isExcluded(strippedAnchorLink)) {
							continue;
						}
						Url url = new Url();
						url.setUrl(strippedAnchorLink);
						urls.add(url);
					} catch (Exception e) {
						handleException(indexableInternet, e);
					}
				}
			}
		} catch (UnsupportedEncodingException e) {
			handleException(indexableInternet, e);
		} catch (IOException e) {
			handleException(indexableInternet, e);
		}
		return urls;
	}

}