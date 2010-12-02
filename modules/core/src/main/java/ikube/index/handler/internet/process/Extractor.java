package ikube.index.handler.internet.process;

import ikube.IConstants;
import ikube.model.Cache;
import ikube.model.IndexContext;
import ikube.model.IndexableInternet;
import ikube.model.Url;
import ikube.toolkit.UriUtilities;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.List;

import javax.swing.text.html.HTML;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.Tag;

import org.apache.log4j.Logger;

public class Extractor {

	private Logger logger = Logger.getLogger(this.getClass());

	protected void extractLinks(IndexContext indexContext, IndexableInternet indexable, Url baseUrl, InputStream inputStream)
			throws Exception {
		// Extract the links
		Reader reader = new InputStreamReader(inputStream, IConstants.ENCODING);
		Source source = new Source(reader);
		List<Tag> tags = source.getAllTags();
		URI baseUri = new URI(baseUrl.getUrl());
		String baseHost = indexable.getUri().getHost();
		for (Tag tag : tags) {
			if (tag.getName().equals(HTMLElementName.A)) {
				if (StartTag.class.isAssignableFrom(tag.getClass())) {
					Attribute attribute = ((StartTag) tag).getAttributes().get(HTML.Attribute.HREF.toString());
					if (attribute != null) {
						try {
							String link = attribute.getValue();
							if (link == null) {
								continue;
							}
							if (UriUtilities.isExcluded(link.trim().toLowerCase())) {
								continue;
							}
							URI uri = UriUtilities.resolve(baseUri, link);
							String resolvedLink = uri.toString();
							if (!UriUtilities.isInternetProtocol(resolvedLink)) {
								continue;
							}
							if (!resolvedLink.contains(baseHost)) {
								continue;
							}
							String replacement = resolvedLink.contains("?") ? "?" : "";
							String strippedSessionLink = UriUtilities.stripJSessionId(resolvedLink, replacement);
							String strippedAnchorLink = UriUtilities.stripAnchor(strippedSessionLink, "");
							Url newUrl = new Url();
							newUrl.setUrl(strippedAnchorLink);
							newUrl.setName(indexable.getName());
							newUrl.setIndexed(Boolean.FALSE);

							Cache cache = indexContext.getCache();
							cache.setUrl(newUrl);
						} catch (Exception e) {
							logger.error("Exception extracting link : " + tag, e);
						}
					}
				}
			}
		}
	}

}
