package ikube.toolkit;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Stack;

public class UriUtilities {

	/**
	 * Resolves a URI reference against a base URI. Work-around for bug in java.net.URI
	 * (<http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4708535>)
	 *
	 * @param baseURI
	 *            the base URI
	 * @param reference
	 *            the URI reference
	 * @return the resulting URI
	 */
	public static URI resolve(final URI baseURI, final String reference) {
		return resolve(baseURI, URI.create(reference));
	}

	/**
	 * Resolves a URI reference against a base URI. Work-around for bugs in java.net.URI (e.g.
	 * <http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4708535>)
	 *
	 * @param baseURI
	 *            the base URI
	 * @param reference
	 *            the URI reference
	 * @return the resulting URI
	 */
	public static URI resolve(final URI baseURI, URI reference) {
		if (baseURI == null) {
			throw new IllegalArgumentException("Base URI may nor be null");
		}
		if (reference == null) {
			throw new IllegalArgumentException("Reference URI may nor be null");
		}
		String s = reference.toString();
		if (s.charAt(0) == '?') {
			return resolveReferenceStartingWithQueryString(baseURI, reference);
		}
		boolean emptyReference = s.length() == 0;
		if (emptyReference) {
			reference = URI.create("#");
		}
		URI resolved = baseURI.resolve(reference);
		if (emptyReference) {
			String resolvedString = resolved.toString();
			resolved = URI.create(resolvedString.substring(0, resolvedString.indexOf('#')));
		}
		return removeDotSegments(resolved);
	}

	/**
	 * Removes dot segments according to RFC 3986, section 5.2.4
	 *
	 * @param uri
	 *            the original URI
	 * @return the URI without dot segments
	 */
	private static URI removeDotSegments(URI uri) {
		String path = uri.getPath();
		if ((path == null) || (path.indexOf("/.") == -1)) {
			// No dot segments to remove
			return uri;
		}
		String[] inputSegments = path.split("/");
		Stack<String> outputSegments = new Stack<String>();
		for (int i = 0; i < inputSegments.length; i++) {
			if ((inputSegments[i].length() == 0) || (".".equals(inputSegments[i]))) {
				// Do nothing
			} else if ("..".equals(inputSegments[i])) {
				if (!outputSegments.isEmpty()) {
					outputSegments.pop();
				}
			} else {
				outputSegments.push(inputSegments[i]);
			}
		}
		StringBuilder outputBuffer = new StringBuilder();
		for (String outputSegment : outputSegments) {
			outputBuffer.append('/').append(outputSegment);
		}
		try {
			return new URI(uri.getScheme(), uri.getAuthority(), outputBuffer.toString(), uri.getQuery(), uri.getFragment());
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Resolves a reference starting with a query string.
	 *
	 * @param baseURI
	 *            the base URI
	 * @param reference
	 *            the URI reference starting with a query string
	 * @return the resulting URI
	 */
	private static URI resolveReferenceStartingWithQueryString(final URI baseURI, final URI reference) {
		String baseUri = baseURI.toString();
		baseUri = baseUri.indexOf('?') > -1 ? baseUri.substring(0, baseUri.indexOf('?')) : baseUri;
		return URI.create(baseUri + reference.toString());
	}

}
