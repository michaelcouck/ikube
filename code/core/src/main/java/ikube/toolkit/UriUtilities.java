package ikube.toolkit;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Stack;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public final class UriUtilities {

	protected static final Logger LOGGER = Logger.getLogger(UriUtilities.class);

	/**
	 * Singularity.
	 */
	private UriUtilities() {
		// Documented
	}

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
	 * Resolves a URI reference against a base URI. Work-around for bug in java.net.URI (<http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4708535>)
	 * 
	 * @param baseURI the base URI
	 * @param reference the URI reference
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
	 * Resolves a URI reference against a base URI. Work-around for bugs in java.net.URI (e.g. <http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4708535>)
	 * 
	 * @param baseURI the base URI
	 * @param reference the URI reference
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
		URI newReference = reference;
		boolean emptyReference = s.length() == 0;
		if (emptyReference) {
			newReference = URI.create("#");
		}
		URI resolved = baseURI.resolve(newReference);
		if (emptyReference) {
			String resolvedString = resolved.toString();
			resolved = URI.create(resolvedString.substring(0, resolvedString.indexOf('#')));
		}
		return removeDotSegments(resolved);
	}

	/**
	 * Removes dot segments according to RFC 3986, section 5.2.4
	 * 
	 * @param uri the original URI
	 * @return the URI without dot segments
	 */
	public static URI removeDotSegments(final URI uri) {
		String path = uri.getPath();
		if ((path == null) || (path.indexOf("/.") == -1)) {
			// No dot segments to remove
			return uri;
		}
		StringBuilder outputBuffer = removeDotSegments(path);
		try {
			return new URI(uri.getScheme(), uri.getAuthority(), outputBuffer.toString(), uri.getQuery(), uri.getFragment());
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static StringBuilder removeDotSegments(String path) {
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
		return outputBuffer;
	}

	/**
	 * Resolves a reference starting with a query string.
	 * 
	 * @param baseURI the base URI
	 * @param reference the URI reference starting with a query string
	 * @return the resulting URI
	 */
	private static URI resolveReferenceStartingWithQueryString(final URI baseURI, final URI reference) {
		String baseUri = baseURI.toString();
		baseUri = baseUri.indexOf('?') > -1 ? baseUri.substring(0, baseUri.indexOf('?')) : baseUri;
		return URI.create(baseUri + reference.toString());
	}

	public static boolean isExcluded(final String string) {
		if (string == null) {
			return Boolean.TRUE;
		}
		// Blank link is useless
		if ("".equals(string)) {
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
		String lowerCaseString = string.toLowerCase(Locale.getDefault());
		return EXCLUDED_PATTERN.matcher(lowerCaseString).matches();
	}

	public static boolean isInternetProtocol(final String string) {
		return PROTOCOL_PATTERN.matcher(string).matches();
	}

	public static String stripJSessionId(final String string, final String replacement) {
		return JSESSIONID_PATTERN.matcher(string).replaceAll(replacement);
	}

	public static String stripAnchor(final String string, final String replacement) {
		return ANCHOR_PATTERN.matcher(string).replaceAll(replacement);
	}

	public static String stripBlanks(final String string) {
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

	public static String stripCarriageReturn(final String string) {
		return CARRIAGE_LINE_FEED_PATTERN.matcher(string).replaceAll("");
	}

	public static String buildUri(final String protocol, final String host, final int port, final String path) {
		try {
			URL url = new URL(protocol, host, port, path);
			return url.toString();
		} catch (Exception e) {
			LOGGER.error("Exception building the url : " + protocol + ", " + host + ", " + port + ", " + path, e);
		}
		return null;
	}

	/**
	 * This method will get the ip address of the machine. If the machine is connected to the net then the first ip that is not the home interface, i.e. not the
	 * localhost which is not particularly useful in a cluster. So essentially we are looking for the ip that looks like 192.... or 10.215.... could be the real
	 * ip from the DNS on the ISP servers of course, but not 127.0.0.1, or on Linux 127.0.1.1 it turns out.
	 * 
	 * @return the first ip address that is not the localhost, something meaningful
	 */
	public static String getIp() {
		Pattern ipPattern = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");
		Enumeration<NetworkInterface> networkInterfaces;
		try {
			networkInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			LOGGER.error("No interfaces? Connected to anything?", e);
			throw new RuntimeException("Couldn't access the interfaces of this machine : ");
		}
		String ip = "127.0.0.1";
		String linuxIp = "127.0.1.1";
		String localhost = "localhost";
		// This is the preferred ip address for the machine
		String networkAssignedIp = "192.168";
		outer: while (networkInterfaces.hasMoreElements()) {
			NetworkInterface networkInterface = networkInterfaces.nextElement();
			Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
			while (inetAddresses.hasMoreElements()) {
				InetAddress inetAddress = inetAddresses.nextElement();
				String hostAddress = inetAddress.getHostAddress();
				if (hostAddress.equals(ip) || hostAddress.equals(linuxIp) || hostAddress.equals(localhost)) {
					// If the ip address is localhost then just continue
					continue;
				}
				try {
					if (!isReachable(inetAddress, 1000)) {
						continue;
					}
					if (hostAddress.startsWith(networkAssignedIp)) {
						// The preferred ip address
						ip = hostAddress;
						break outer;
					} else if (ipPattern.matcher(hostAddress).matches()) {
						ip = hostAddress;
					}
				} catch (IOException e) {
					LOGGER.error("Exception checking the ip address : " + hostAddress, e);
					continue;
				}
			}
		}
		LOGGER.info("Ip address : " + ip);
		return ip;
	}

	private static boolean isReachable(final InetAddress inetAddress, final int timeout) throws IOException {
		return inetAddress.isReachable(timeout);
	}

}