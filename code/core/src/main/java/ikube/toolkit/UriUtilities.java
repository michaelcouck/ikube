package ikube.toolkit;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Stack;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class UriUtilities {

	protected static Logger LOGGER = Logger.getLogger(UriUtilities.class);

	/** Accepted protocols. */
	protected static final Pattern PROTOCOL_PATTERN;
	/** The pattern regular expression to match a url. */
	protected static final Pattern EXCLUDED_PATTERN;
	/** The pattern to strip the JSessionId form the urls. */
	protected static final Pattern JSESSIONID_PATTERN;
	/** The anchor pattern. */
	protected static final Pattern ANCHOR_PATTERN;
	/** The carriage return/line feed pattern. */
	protected static final Pattern CARRIAGE_LINE_FEED_PATTERN;

	static {
		ANCHOR_PATTERN = Pattern.compile("#[^#]*$");
		CARRIAGE_LINE_FEED_PATTERN = Pattern.compile("[\n\r]");
		PROTOCOL_PATTERN = Pattern.compile("(http).*|(www).*|(https).*|(ftp).*");
		EXCLUDED_PATTERN = Pattern.compile("^news.*|^javascript.*|^mailto.*|^plugintest.*|^skype.*");
		JSESSIONID_PATTERN = Pattern.compile("([;_]?((?i)l|j|bv_)?((?i)sid|phpsessid|sessionid)=.*?)(\\?|&amp;|#|$)");
	}

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
	public static String resolve(final URI baseURI, final String reference) {
		StringBuilder builder = new StringBuilder();
		// Strip the space characters from the reference
		String trimmedReference = UriUtilities.stripBlanks(reference);
		int index = trimmedReference.indexOf('?');
		URI uri = null;
		String query = "";
		if (index > -1) {
			query = trimmedReference.substring(index);
			String strippedReference = trimmedReference.substring(0, index);
			uri = URI.create(strippedReference);
		} else {
			uri = URI.create(trimmedReference);
		}

		URI resolved = resolve(baseURI, uri);

		builder.append(resolved.toString());
		builder.append(query);

		return builder.toString();
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
			throw new IllegalArgumentException("Base URI may not be null");
		}
		if (reference == null) {
			throw new IllegalArgumentException("Reference URI may not be null");
		}
		String s = reference.toString();
		if (s == null || s.length() == 0) {
			return URI.create(s);
		}
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

	public static boolean isExcluded(String string) {
		if (string == null) {
			return Boolean.TRUE;
		}
		// Blank link is useless
		if (string.equals("")) {
			return Boolean.TRUE;
		}
		// Check that there is at least one character in the link
		char[] chars = string.toCharArray();
		boolean containsCharacters = Boolean.FALSE;
		for (char c : chars) {
			if (Character.isLetterOrDigit(c)) {
				containsCharacters = Boolean.TRUE;
				break;
			}
		}
		if (!containsCharacters) {
			return Boolean.TRUE;
		}
		String lowerCaseString = string.toLowerCase();
		return EXCLUDED_PATTERN.matcher(lowerCaseString).matches();
	}

	public static boolean isInternetProtocol(String string) {
		return PROTOCOL_PATTERN.matcher(string).matches();
	}

	public static String stripJSessionId(String string, String replacement) {
		return JSESSIONID_PATTERN.matcher(string).replaceAll(replacement);
	}

	public static String stripAnchor(String string, String replacement) {
		return ANCHOR_PATTERN.matcher(string).replaceAll(replacement);
	}

	public static String stripBlanks(String string) {
		StringBuilder builder = new StringBuilder();
		char[] chars = string.toCharArray();
		for (char c : chars) {
			if (Character.isWhitespace(c) || Character.isSpaceChar(c)) {
				continue;
			}
			builder.append(c);
		}
		return builder.toString();
	}

	public static String stripCarriageReturn(String string) {
		return CARRIAGE_LINE_FEED_PATTERN.matcher(string).replaceAll("");
	}

	public static String buildUri(String protocol, String host, int port, String path) {
		try {
			URL url = new URL(protocol, host, port, path);
			return url.toString();
		} catch (Exception e) {
			LOGGER.error("Exception building the url : " + protocol + ", " + host + ", " + port + ", " + path, e);
		}
		return null;
	}

}