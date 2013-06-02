package ikube.action.index.handler;

import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.model.Indexable;
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
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.text.html.HTML;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.Tag;

public class IndexableInternetHandler extends IndexableHandler<IndexableInternet> {

	static class ResourceManager implements IResourceProvider<Url> {

		static Stack<Url> URLS = new Stack<Url>();
		static Set<Long> DONE = new TreeSet<Long>();

		ResourceManager(final IndexableInternet indexableInternet) {
			Url url = new Url();
			url.setIndexed(Boolean.FALSE);
			url.setUrl(indexableInternet.getUrl());
			url.setName(indexableInternet.getName());
			url.setUrlId(HashUtilities.hash(indexableInternet.getUrl()).longValue());
			URLS.add(url);
		}

		public Url getResource() {
			if (URLS.size() == 0) {
				return null;
			}
			Url url = URLS.pop();
			DONE.add(url.getUrlId());
			return url;
		}
		
		public void setResource(final Url resource) {
			Long hash = HashUtilities.hash(resource.getUrl());
			if (!DONE.contains(hash)) {
				Url url = new Url();
				url.setUrlId(hash);
				url.setUrl(resource.getUrl());
				url.setIndexed(Boolean.FALSE);
				URLS.add(url);
			}
		}

	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Future<?>> handleIndexable(final IndexContext<?> indexContext, final IndexableInternet indexableInternet) throws Exception {
		final AtomicInteger threads = new AtomicInteger(indexableInternet.getThreads());
		ForkJoinPool forkJoinPool = new ForkJoinPool(threads.get());
		ResourceManager resourceManager = new ResourceManager(indexableInternet);
		Future<?> recursiveAction = getRecursiveAction(indexContext, indexableInternet, resourceManager);
		forkJoinPool.invoke((RecursiveAction) recursiveAction);
		return new ArrayList<Future<?>>(Arrays.asList(recursiveAction));
	}

	@Override
	protected void handleResource(final IndexContext<?> indexContext, final Indexable<?> indexable, final Object resource) {
		logger.info("Handling resource : " + resource + ", thread : " + Thread.currentThread().hashCode());
		try {
			extractLinksFromContent((IndexableInternet) indexable, new URL(((Url) resource).getUrl()).openStream());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void extractLinksFromContent(final IndexableInternet indexableInternet, final InputStream inputStream) {
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
						// ResourceManager.setUrl(strippedAnchorLink);
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
	}

}