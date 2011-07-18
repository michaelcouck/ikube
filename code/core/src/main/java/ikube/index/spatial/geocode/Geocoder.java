package ikube.index.spatial.geocode;

import ikube.IConstants;
import ikube.index.spatial.Coordinate;
import ikube.service.ISearcherWebService;
import ikube.service.ServiceLocator;
import ikube.toolkit.SerializationUtilities;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;

/**
 * @see IGeocoder
 * @author Michael Couck
 * @since 06.03.11
 * @version 01.00
 */
public class Geocoder implements IGeocoder {

	private static final Logger LOGGER = Logger.getLogger(Geocoder.class);

	private String searchUrl;
	private String[] searchStrings;
	private String[] searchFields;
	private ISearcherWebService searchRemote;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Coordinate getCoordinate(String address) {
		try {
			// Get the GeoSpatial search service
			if (searchRemote == null) {
				searchRemote = ServiceLocator.getService(ISearcherWebService.class, searchUrl, ISearcherWebService.NAMESPACE,
						ISearcherWebService.SERVICE);
				if (searchRemote == null) {
					LOGGER.warn("Searcher web service not available : "
							+ ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE));
					return null;
				}
			}
			// Trim the address for strange characters to get a better result
			String trimmedAddress = StringUtils.trim(address);
			Arrays.fill(searchStrings, trimmedAddress);
			String results = searchRemote.searchMulti(IConstants.GEOSPATIAL, searchStrings, searchFields, Boolean.TRUE, 0, 10);
			List<Map<String, String>> list = (List<Map<String, String>>) SerializationUtilities.deserialize(results);
			Map<String, String> firstResult = list.size() >= 2 ? list.get(0) : null;
			if (firstResult != null) {
				// We got a result, so we'll rely on Lucene to provide the best match for
				// the address according to the data from GeoNames
				String latitude = firstResult.get(IConstants.LATITUDE);
				String longitude = firstResult.get(IConstants.LONGITUDE);
				double lat = Double.parseDouble(latitude);
				double lng = Double.parseDouble(longitude);
				return new Coordinate(lat, lng, trimmedAddress);
			}
		} catch (Exception e) {
			String message = "Exception accessing the spatial search service : " + address
					+ ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
			LOGGER.error(message, e);
		}
		return null;
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
	 * @param SEARCH_FIELDS
	 *            the search fields to search in the GeoSpatial index, typically this will be the name field because this is an aggregation
	 *            of the name of the feature in the GeoNames data and the enriched foelds for the city and the country
	 */
	public void setSearchFields(List<String> searchFields) {
		this.searchFields = searchFields.toArray(new String[searchFields.size()]);
		this.searchStrings = new String[searchFields.size()];
	}

}