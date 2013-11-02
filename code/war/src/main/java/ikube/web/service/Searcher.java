package ikube.web.service;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the base class for all searcher web services, common logic and properties. Also all the methods that are exposed to clients for xml and Json
 * responses are defined here. This class could be seen as the API that is exposed to iKube clients.
 * 
 * @author Michael couck
 * @since 20.11.12
 * @version 01.00
 */
public abstract class Searcher extends Resource {

	public static final String SEARCH = "/search";

	public static final String ALL = "/all";
	public static final String JSON = "/json";
	public static final String SINGLE = "/single";
	public static final String SORTED = "/sorted";
	public static final String GEOSPATIAL = "/geospatial";
	public static final String SORTED_TYPED = "/sorted/typed";

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	/** Basic */
	public abstract Response search(final String indexName, final String searchStrings, final String searchFields, final boolean fragment,
			final int firstResult, final int maxResults);

	/** Sorted */
	public abstract Response search(final String indexName, final String searchStrings, final String searchFields, final String sortFields,
			final boolean fragment, final int firstResult, final int maxResults);

	/** Sorted and typed */
	public abstract Response search(final String indexName, final String searchStrings, final String searchFields, final String typeFields,
			final String sortFields, final boolean fragment, final int firstResult, final int maxResults);

	/** Geospatial */
	public abstract Response search(final String indexName, final String searchStrings, final String searchFields, final String typeFields,
			final boolean fragment, final int firstResult, final int maxResults, final int distance, final double latitude, final double longitude);

	/** Search json */
	public abstract Response search(final HttpServletRequest request, final UriInfo uriInfo);

	/** Search Json, all fields in all indexes, just for convenience. */
	public abstract Response searchAll(final HttpServletRequest request, final UriInfo uriInfo);

}