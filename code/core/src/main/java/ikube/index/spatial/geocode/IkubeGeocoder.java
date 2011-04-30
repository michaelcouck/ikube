package ikube.index.spatial.geocode;

import ikube.IConstants;
import ikube.index.spatial.Coordinate;
import ikube.service.ISearcherWebService;
import ikube.service.ServiceLocator;
import ikube.toolkit.SerializationUtilities;

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

	private String url;
	private String indexName;
	private String searchField;

	@Override
	@SuppressWarnings("unchecked")
	public Coordinate getCoordinate(String address) {
		try {
			address = StringUtils.trim(address);
			ISearcherWebService searchRemote = ServiceLocator.getService(ISearcherWebService.class, url, ISearcherWebService.NAMESPACE,
					ISearcherWebService.SERVICE);
			if (searchRemote == null) {
				LOGGER.warn("Searcher web service not available : " + ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE));
				return null;
			}
			String results = searchRemote.searchSingle(indexName, address, searchField, Boolean.TRUE, 0, 10);
			List<Map<String, String>> list = (List<Map<String, String>>) SerializationUtilities.deserialize(results);
			Map<String, String> firstResult = list.size() >= 2 ? list.get(0) : null;
			if (firstResult != null) {
				String latitude = firstResult.get(IConstants.LATITUDE);
				String longitude = firstResult.get(IConstants.LONGITUDE);
				double lat = Double.parseDouble(latitude);
				double lng = Double.parseDouble(longitude);
				return new Coordinate(lat, lng, address);
			}
		} catch (Exception e) {
			String message = "Exception accessing the spatial search service : " + address
					+ ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
			LOGGER.error(message, e);
		}
		return null;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public void setSearchField(String searchField) {
		this.searchField = searchField;
	}

}
