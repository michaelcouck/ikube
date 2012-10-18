package ikube.service;

import ikube.IConstants;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.SerializationUtilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Path looks like this: http://localhost:9080/ikube/service/search/multi
 * 
 * Trial and error:
 * 
 * <pre>
 * Doesn't work.
 * GenericEntity<ArrayList<HashMap<String, String>>> entity = new GenericEntity<ArrayList<HashMap<String, String>>>(results) {
 * 		// Abstract implementation
 * };
 * Doesn't work.
 * return Response.ok().entity(results.toArray(new HashMap[results.size()])).build();
 * Could be a lot of work.
 * MessageBodyWriter<ArrayList<HashMap<String, String>>> messageBodyWriter = null;
 * </pre>
 * 
 * @author Michael couck
 * @since 21.01.12
 * @version 01.00
 */
@Component
@Path(Searcher.SEARCH)
@Scope(Searcher.REQUEST)
@Produces(MediaType.TEXT_PLAIN)
public class Searcher {

	/** Constants for the paths to the web services. */
	public static final String REQUEST = "request";
	public static final String SERVICE = "/service";
	public static final String SEARCH = "/search";
	public static final String SINGLE = "/single";
	public static final String MULTI = "/multi";
	public static final String MULTI_SORTED = "/multi/sorted";
	public static final String MULTI_ALL = "/multi/all";
	public static final String MULTI_SPATIAL = "/multi/spatial";
	public static final String MULTI_SPATIAL_ALL = "/multi/spatial/all";
	public static final String MULTI_SPATIAL_ALL_TABLE = "/multi/spatial/all/table";

	public static final String RESULTS_TO_TABLE = "/table";

	private static final Logger LOGGER = LoggerFactory.getLogger(Searcher.class);

	@Autowired
	private ISearcherService searcherService;

	/**
	 * Does a search on a single field on the index defined in the parameter list.
	 * 
	 * @param indexName the name of the index to search
	 * @param searchString the search string to search for
	 * @param searchField the search field in the index
	 * @param fragment whether to add the text fragments to the results
	 * @param firstResult the start document in the index, for paging
	 * @param maxResults the end document in the index, also for paging
	 * @return a serialized string of the results from the search
	 */
	@GET
	@Path(Searcher.SINGLE)
	@Consumes(MediaType.APPLICATION_XML)
	public String searchSingle(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings,
			@QueryParam(value = IConstants.SEARCH_FIELDS) final String searchFields,
			@QueryParam(value = IConstants.FRAGMENT) final boolean fragment,
			@QueryParam(value = IConstants.FIRST_RESULT) final int firstResult,
			@QueryParam(value = IConstants.MAX_RESULTS) final int maxResults) {
		ArrayList<HashMap<String, String>> results = searcherService.searchSingle(indexName, searchStrings, searchFields, fragment,
				firstResult, maxResults);
		return SerializationUtilities.serialize(results);
	}

	/**
	 * Does a search on multiple fields and multiple search strings.
	 * 
	 * @param indexName the name of the index to search
	 * @param searchStrings the search strings to search for
	 * @param searchFields the search fields in the index
	 * @param fragment whether to add the text fragments to the results
	 * @param firstResult the start document in the index, for paging
	 * @param maxResults the end document in the index, also for paging
	 * @return a serialized string of the results from the search
	 */
	@GET
	@Path(Searcher.MULTI)
	@Consumes(MediaType.APPLICATION_XML)
	public String searchMulti(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings,
			@QueryParam(value = IConstants.SEARCH_FIELDS) final String searchFields,
			@QueryParam(value = IConstants.FRAGMENT) final boolean fragment,
			@QueryParam(value = IConstants.FIRST_RESULT) final int firstResult,
			@QueryParam(value = IConstants.MAX_RESULTS) final int maxResults) {
		String[] searchStringsArray = StringUtils.split(searchStrings, IConstants.SEMI_COLON);
		String[] searchFieldsArray = StringUtils.split(searchFields, IConstants.SEMI_COLON);
		ArrayList<HashMap<String, String>> results = searcherService.searchMulti(indexName, searchStringsArray, searchFieldsArray,
				fragment, firstResult, maxResults);
		return SerializationUtilities.serialize(results);
	}

	/**
	 * Does a search on multiple fields and multiple search strings and sorts the results according the sort fields.
	 * 
	 * @param indexName the name of the index to search
	 * @param searchStrings the search strings to search for
	 * @param searchFields the search fields in the index
	 * @param sortFields the fields to sort the results on
	 * @param fragment whether to add the text fragments to the results
	 * @param firstResult the start document in the index, for paging
	 * @param maxResults the end document in the index, also for paging
	 * @return a serialized string of the results from the search
	 */
	@GET
	@Path(Searcher.MULTI_SORTED)
	@Consumes(MediaType.APPLICATION_XML)
	public String searchMultiSorted(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings,
			@QueryParam(value = IConstants.SEARCH_FIELDS) final String searchFields,
			@QueryParam(value = IConstants.SORT_FIELDS) final String sortFields,
			@QueryParam(value = IConstants.FRAGMENT) final boolean fragment,
			@QueryParam(value = IConstants.FIRST_RESULT) final int firstResult,
			@QueryParam(value = IConstants.MAX_RESULTS) final int maxResults) {
		String[] searchStringsArray = StringUtils.split(searchStrings, IConstants.SEMI_COLON);
		String[] searchFieldsArray = StringUtils.split(searchFields, IConstants.SEMI_COLON);
		String[] sortFieldsArray = StringUtils.split(sortFields, IConstants.SEMI_COLON);
		ArrayList<HashMap<String, String>> results = searcherService.searchMultiSorted(indexName, searchStringsArray, searchFieldsArray,
				sortFieldsArray, fragment, firstResult, maxResults);
		return SerializationUtilities.serialize(results);
	}

	/**
	 * This is a convenient method to search for the specified strings in all the fields.
	 * 
	 * @param indexName the name of the index to search
	 * @param searchStrings the search strings to search for
	 * @param fragment whether to generate a fragment from the stored data for the matches
	 * @param firstResult the first result for paging
	 * @param maxResults the maximum results for paging
	 * @return the results from the search serialized to an xml string
	 */
	@GET
	@Path(Searcher.MULTI_ALL)
	@Consumes(MediaType.APPLICATION_XML)
	public String searchMultiAll(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings,
			@QueryParam(value = IConstants.FRAGMENT) final boolean fragment,
			@QueryParam(value = IConstants.FIRST_RESULT) final int firstResult,
			@QueryParam(value = IConstants.MAX_RESULTS) final int maxResults) {
		String[] searchStringsArray = StringUtils.split(searchStrings, IConstants.SEMI_COLON);
		ArrayList<HashMap<String, String>> results = searcherService.searchMultiAll(indexName, searchStringsArray, fragment, firstResult,
				maxResults);
		String xml = SerializationUtilities.serialize(results);
		// LOGGER.info("Xml : " + xml);
		return xml;
	}

	/**
	 * This method will search for the specified strings in the specified fields, with the usual parameters like whether to generate a
	 * fragment and so on, but will sort the results according to the distance from the co-ordinate that was specified in the parameters
	 * list.
	 * 
	 * @param indexName the name of the index to search
	 * @param searchStrings the search strings to search for
	 * @param searchFields the fields to search through
	 * @param fragment whether to generate a fragment from the stored data for the matches
	 * @param firstResult the first result for paging
	 * @param maxResults the maximum results for paging
	 * @param distance the maximum distance that should be allowed for the results
	 * @param latitude the longitude of the co-ordinate to sort on
	 * @param longitude the latitude of the co-ordinate to sort on
	 * @return the results from the search serialized to an xml string
	 */
	@GET
	@Path(Searcher.MULTI_SPATIAL)
	@Consumes(MediaType.APPLICATION_XML)
	public String searchMultiSpacial(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings,
			@QueryParam(value = IConstants.SEARCH_FIELDS) final String searchFields,
			@QueryParam(value = IConstants.FRAGMENT) final boolean fragment,
			@QueryParam(value = IConstants.FIRST_RESULT) final int firstResult,
			@QueryParam(value = IConstants.MAX_RESULTS) final int maxResults, @QueryParam(value = IConstants.DISTANCE) final int distance,
			@QueryParam(value = IConstants.LATITUDE) final String latitude, @QueryParam(value = IConstants.LONGITUDE) final String longitude) {
		String[] searchStringsArray = StringUtils.split(searchStrings, IConstants.SEMI_COLON);
		String[] searchFieldsArray = StringUtils.split(searchFields, IConstants.SEMI_COLON);
		ArrayList<HashMap<String, String>> results = searcherService.searchMultiSpacial(indexName, searchStringsArray, searchFieldsArray,
				fragment, firstResult, maxResults, distance, Double.parseDouble(latitude), Double.parseDouble(longitude));
		return SerializationUtilities.serialize(results);
	}

	/**
	 * This method will search all the fields in the spatial index, and sort the results by distance from a point.
	 * 
	 * @param indexName the name of the index to search
	 * @param searchStrings the search strings, note that all the search strings will be used to search all the fields
	 * @param fragment whether the results should contain the fragment
	 * @param firstResult the first result to page
	 * @param maxResults the max results to return, for paging
	 * @param distance the distance around the point specified to return results for
	 * @param latitude the latitude for the starting point for sorting the results from, and for the distance calculation
	 * @param longitude the longitude for the starting point for sorting the results from, and for the distance calculation
	 * @return the results around the point specified, going the maximum distance specified, sorted according to the distance from teh point
	 *         specified
	 */
	@GET
	@Path(Searcher.MULTI_SPATIAL_ALL)
	@Consumes(MediaType.APPLICATION_XML)
	public String searchMultiSpacialAll(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings,
			@QueryParam(value = IConstants.FRAGMENT) final boolean fragment,
			@QueryParam(value = IConstants.FIRST_RESULT) final int firstResult,
			@QueryParam(value = IConstants.MAX_RESULTS) final int maxResults, @QueryParam(value = IConstants.DISTANCE) final int distance,
			@QueryParam(value = IConstants.LATITUDE) final String latitude, @QueryParam(value = IConstants.LONGITUDE) final String longitude) {
		String[] searchStringsArray = StringUtils.split(searchStrings, IConstants.SEMI_COLON);
		ArrayList<HashMap<String, String>> results = searcherService.searchMultiSpacialAll(indexName, searchStringsArray, fragment,
				firstResult, maxResults, distance, Double.parseDouble(latitude), Double.parseDouble(longitude));
		return SerializationUtilities.serialize(results);
	}

	/**
	 * This method is a brute force calculation of the shortest route between the points. The more efficient option would be to use an
	 * neural network, but this falls outside the scope of this simple search results example page.
	 */
	ArrayList<HashMap<String, String>> routed(final ArrayList<HashMap<String, String>> results) {
		// These are the points sorted according to the shortest distance to visit them all
		ArrayList<HashMap<String, String>> resultsRouted = new ArrayList<HashMap<String, String>>();
		if (results.size() > 1) {
			HashMap<String, String> topResult = results.get(0);
			HashMap<String, String> statistics = results.remove(results.size() - 1);
			resultsRouted.add(topResult);
			do {
				// Recursively find the closest result to the top result and add it to the sorted array
				HashMap<String, String> bestResult = null;
				double bestDistance = Long.MAX_VALUE;
				for (final HashMap<String, String> nextResult : results) {
					if (topResult == nextResult || resultsRouted.contains(nextResult)) {
						continue;
					}
					double lat1 = Double.parseDouble(topResult.get(IConstants.LATITUDE));
					double lon1 = Double.parseDouble(topResult.get(IConstants.LONGITUDE));
					double lat2 = Double.parseDouble(nextResult.get(IConstants.LATITUDE));
					double lon2 = Double.parseDouble(nextResult.get(IConstants.LONGITUDE));
					double nextDistance = distance(lat1, lon1, lat2, lon2, 'K');
					if (nextDistance < bestDistance) {
						bestDistance = nextDistance;
						bestResult = nextResult;
					}
				}
				resultsRouted.add(bestResult);
				topResult = bestResult;
			} while (resultsRouted.size() < results.size());
			results.add(statistics);
			resultsRouted.add(statistics);
		}
		return resultsRouted;
	}

	/* :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	/* :: : */
	/* :: This routine calculates the distance between two points (given the : */
	/* :: latitude/longitude of those points). It is being used to calculate : */
	/* :: the distance between two ZIP Codes or Postal Codes using our : */
	/* :: ZIPCodeWorld(TM) and PostalCodeWorld(TM) products. : */
	/* :: : */
	/* :: Definitions: : */
	/* :: South latitudes are negative, east longitudes are positive : */
	/* :: : */
	/* :: Passed to function: : */
	/* :: lat1, lon1 = Latitude and Longitude of point 1 (in decimal degrees) : */
	/* :: lat2, lon2 = Latitude and Longitude of point 2 (in decimal degrees) : */
	/* :: unit = the unit you desire for results : */
	/* :: where: 'M' is statute miles : */
	/* :: 'K' is kilometers (default) : */
	/* :: 'N' is nautical miles : */
	/* :: United States ZIP Code/ Canadian Postal Code databases with latitude & : */
	/* :: longitude are available at http://www.zipcodeworld.com : */
	/* :: : */
	/* :: For enquiries, please contact sales@zipcodeworld.com : */
	/* :: : */
	/* :: Official Web site: http://www.zipcodeworld.com : */
	/* :: : */
	/* :: Hexa Software Development Center © All Rights Reserved 2004 : */
	/* :: : */
	/* :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	double distance(double lat1, double lon1, double lat2, double lon2, char unit) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
				* Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (unit == 'K') {
			dist = dist * 1.609344;
		} else if (unit == 'N') {
			dist = dist * 0.8684;
		}
		return (dist);
	}

	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	/* :: This function converts decimal degrees to radians : */
	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	/* :: This function converts radians to decimal degrees : */
	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
	}

	@GET
	@Deprecated
	@Path(Searcher.MULTI_SPATIAL_ALL_TABLE)
	@Consumes(MediaType.APPLICATION_XML)
	public String searchMultiSpacialAllFormat(@QueryParam(value = IConstants.INDEX_NAME) final String indexName,
			@QueryParam(value = IConstants.SEARCH_STRINGS) final String searchStrings,
			@QueryParam(value = IConstants.FRAGMENT) final boolean fragment,
			@QueryParam(value = IConstants.FIRST_RESULT) final int firstResult,
			@QueryParam(value = IConstants.MAX_RESULTS) final int maxResults, @QueryParam(value = IConstants.DISTANCE) final int distance,
			@QueryParam(value = IConstants.LATITUDE) final String latitude,
			@QueryParam(value = IConstants.LONGITUDE) final String longitude, @QueryParam(value = IConstants.EXCLUDED) final String excluded) {
		String[] searchStringsArray = StringUtils.split(searchStrings, IConstants.SEMI_COLON);
		ArrayList<HashMap<String, String>> results = searcherService.searchMultiSpacialAll(indexName, searchStringsArray, fragment,
				firstResult, maxResults, distance, Double.parseDouble(latitude), Double.parseDouble(longitude));
		excludeFields(results, excluded);
		return formatToHtml(results);
	}

	/**
	 * This is not properly tested.
	 * 
	 * This method will just format the results to an html table for convenience and a parameter to filter out fields that are not to be
	 * included in the table.
	 * 
	 * @param xml the xml results, i.e. a list of maps
	 * @param excluded the delimited string of fields to be excluded from the results
	 * @return the html table from the results
	 * @throws IOException
	 */
	@POST
	@Deprecated
	@SuppressWarnings("unchecked")
	@Path(Searcher.RESULTS_TO_TABLE)
	@Consumes(MediaType.APPLICATION_XML)
	public String formatToHtmlTable(@QueryParam(value = "excluded") final String excluded,
			@Context final HttpServletRequest httpServletRequest) throws IOException {
		// LOGGER.info("Format : ");
		String xml = FileUtilities.getContents(httpServletRequest.getInputStream(), Integer.MAX_VALUE).toString();
		// LOGGER.info("Format : " + xml);
		ArrayList<HashMap<String, String>> results = (ArrayList<HashMap<String, String>>) SerializationUtilities.deserialize(xml);
		// LOGGER.info("Format : " + xml + ", " + results);
		// Strip the fields in the results that are to be excluded
		excludeFields(results, excluded);
		return formatToHtml(results);
	}

	private void excludeFields(final ArrayList<HashMap<String, String>> results, final String excluded) {
		if (StringUtils.isEmpty(excluded)) {
			return;
		}
		String[] excludedFields = StringUtils.split(excluded, ",;|:");
		for (int i = 0; i < results.size() - 1; i++) {
			HashMap<String, String> result = results.get(i);
			for (String excludedField : excludedFields) {
				result.remove(excludedField);
			}
		}
	}

	private String formatToHtml(final ArrayList<HashMap<String, String>> results) {
		Document document = DocumentFactory.getInstance().createDocument();
		Element table = document.addElement("table");
		if (results.size() > 1) {
			Element columns = table.addElement("tr");
			HashMap<String, String> resultOne = results.get(0);
			for (Map.Entry<String, String> mapEntry : resultOne.entrySet()) {
				Element column = columns.addElement("th");
				column.setText(mapEntry.getKey());
			}
			for (int i = 0; i < results.size() - 1; i++) {
				Element row = table.addElement("tr");
				HashMap<String, String> result = results.get(i);
				for (Map.Entry<String, String> mapEntry : result.entrySet()) {
					Element data = row.addElement("td");
					data.setText(mapEntry.getValue());
				}
			}
		}
		try {
			// Pretty print the document
			ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
			OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter writer = new XMLWriter(arrayOutputStream, format);
			writer.write(document);
			String html = StringEscapeUtils.unescapeXml(arrayOutputStream.toString());
			return html;
		} catch (Exception e) {
			LOGGER.error("Exception pretty printing : ", e);
		}
		return StringEscapeUtils.unescapeXml(document.asXML());
	}

}