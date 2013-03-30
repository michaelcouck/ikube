package ikube.action.index.handler.enrich.geocode;

import ikube.IConstants;
import ikube.security.WebServiceAuthentication;
import ikube.toolkit.SerializationUtilities;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * @see IGeocoder
 * @author Michael Couck
 * @since 06.03.11
 * @version 01.00
 */
public class Geocoder implements IGeocoder, InitializingBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(Geocoder.class);

	private String userid;
	private String password;
	private String searchUrl;
	private HttpClient httpClient;
	private String[] searchStrings;
	private String[] searchFields;
	private NameValuePair searchFieldsPair;
	private NameValuePair firstResultPair;
	private NameValuePair maxResultsPair;
	private NameValuePair fragmentPair;
	private NameValuePair indexNamePair;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Coordinate getCoordinate(String address) {
		try {
			// Trim the address for strange characters to get a better result
			String trimmedAddress = StringUtils.trim(address);
			Arrays.fill(this.searchStrings, trimmedAddress);

			// Get the GeoSpatial search service
			NameValuePair searchStringsPair = new NameValuePair(IConstants.SEARCH_STRINGS, StringUtils.join(this.searchStrings,
					IConstants.SEMI_COLON));

			GetMethod getMethod = new GetMethod(searchUrl);
			NameValuePair[] params = new NameValuePair[] { indexNamePair, searchStringsPair, searchFieldsPair, fragmentPair,
					firstResultPair, maxResultsPair };
			getMethod.setQueryString(params);
			int result = httpClient.executeMethod(getMethod);
			String xml = getMethod.getResponseBodyAsString();
			LOGGER.info("Result from web service : " + result + ", " + xml);

			ArrayList<HashMap<String, String>> results = (ArrayList<HashMap<String, String>>) SerializationUtilities.deserialize(xml);
			if (results.size() >= 2) {
				Map<String, String> firstResult = results.get(0);
				// We got a result, so we'll rely on Lucene to provide the best match for
				// the address according to the data from GeoNames
				String latitude = firstResult.get(IConstants.LATITUDE);
				String longitude = firstResult.get(IConstants.LONGITUDE);
				if (latitude != null && longitude != null) {
					double lat = Double.parseDouble(latitude);
					double lng = Double.parseDouble(longitude);
					return new Coordinate(lat, lng, trimmedAddress);
				}
				LOGGER.info("Result from geoname search : " + firstResult);
			}
		} catch (Exception e) {
			LOGGER.error("Address and geocoder : ", address, toString());
			LOGGER.error("Exception accessing the spatial search service : ", e);
		}
		return null;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		firstResultPair = new NameValuePair(IConstants.FIRST_RESULT, "0");
		maxResultsPair = new NameValuePair(IConstants.MAX_RESULTS, "10");
		fragmentPair = new NameValuePair(IConstants.FRAGMENT, Boolean.TRUE.toString());
		indexNamePair = new NameValuePair(IConstants.INDEX_NAME, IConstants.GEOSPATIAL);
		searchFieldsPair = new NameValuePair(IConstants.SEARCH_FIELDS, StringUtils.join(this.searchFields, IConstants.SEMI_COLON));
		httpClient = new HttpClient();
		URL url;
		try {
			url = new URL(searchUrl);
			WebServiceAuthentication.authenticate(httpClient, url.getHost(), url.getPort(), userid, password);
		} catch (MalformedURLException e) {
			LOGGER.error(null, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSearchUrl(String searchUrl) {
		this.searchUrl = searchUrl;
	}

	/**
	 * This sets the search fields. At the time of writing the fields that were indexed in the GeoNames data was 'name', 'city' and
	 * 'country'. The city and country fields are in fact the enriched data. Essentially all three of these fields will be searched, in
	 * order and the best match for them aggregated will be used for the results.
	 * 
	 * @param searchFields the search fields to search in the GeoSpatial index, typically this will be the name field because this is an
	 *            aggregation of the name of the feature in the GeoNames data and the enriched fields for the city and the country
	 */
	public void setSearchFields(List<String> searchFields) {
		this.searchFields = searchFields.toArray(new String[searchFields.size()]);
		this.searchStrings = new String[searchFields.size()];
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}