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
 * @author Michael Couck
 * @since 06.03.11
 * @version 01.00
 */
public class IkubeGeocoder implements IGeocoder {

	private static final Logger LOGGER = Logger.getLogger(IkubeGeocoder.class);

	private String searchUrl;
	private String[] searchStrings;
	private String[] searchFields;
	private ISearcherWebService searchRemote;

	@Override
	@SuppressWarnings("unchecked")
	public Coordinate getCoordinate(String address) {
		try {
			if (searchRemote == null) {
				searchRemote = ServiceLocator.getService(ISearcherWebService.class, searchUrl, ISearcherWebService.NAMESPACE,
						ISearcherWebService.SERVICE);
				if (searchRemote == null) {
					LOGGER.warn("Searcher web service not available : "
							+ ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE));
					return null;
				}
			}
			String trimmedAddress = StringUtils.trim(address);
			Arrays.fill(searchStrings, trimmedAddress);
			String results = searchRemote.searchMulti(IConstants.GEOSPATIAL, searchStrings, searchFields, Boolean.TRUE, 0, 10);
			List<Map<String, String>> list = (List<Map<String, String>>) SerializationUtilities.deserialize(results);
			Map<String, String> firstResult = list.size() >= 2 ? list.get(0) : null;
			if (firstResult != null) {
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

	public void setSearchUrl(String searchUrl) {
		this.searchUrl = searchUrl;
	}

	public void setSearchFields(List<String> searchFields) {
		this.searchFields = searchFields.toArray(new String[searchFields.size()]);
		this.searchStrings = new String[searchFields.size()];
	}

}
