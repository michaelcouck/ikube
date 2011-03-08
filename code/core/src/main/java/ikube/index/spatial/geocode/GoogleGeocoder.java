package ikube.index.spatial.geocode;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import ikube.IConstants;
import ikube.index.spatial.Coordinate;
import ikube.model.Indexable;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.XmlUtilities;

/**
 * @author Michael Couck
 * @since 06.03.11
 * @version 01.00
 */
public class GoogleGeocoder implements IGeocoder {

	/** Geocode API and parameters. */
	protected static final String ADDRESS = "address";
	protected static final String SENSOR = "sensor";

	private Logger logger = Logger.getLogger(this.getClass());
	private String geoCodeApi;

	@Override
	public Coordinate getCoordinate(Indexable<?> indexable) {
		try {
			String address = buildAddress(indexable, new StringBuilder()).toString();
			// Call the geocoder with the address
			String uri = getUri(address);
			URL url = new URL(uri);
			String xml = FileUtilities.getContents(url.openStream(), Integer.MAX_VALUE).toString();
			InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
			Element rootElement = XmlUtilities.getDocument(inputStream, IConstants.ENCODING).getRootElement();
			Element element = XmlUtilities.getElement(rootElement, IConstants.LOCATION);
			Element latitudeElement = XmlUtilities.getElement(element, IConstants.LAT);
			Element longitudeElement = XmlUtilities.getElement(element, IConstants.LNG);
			double lat = Double.parseDouble(latitudeElement.getText());
			double lng = Double.parseDouble(longitudeElement.getText());
			Coordinate coordinate = new Coordinate(lat, lng, address);
			return coordinate;
		} catch (Exception e) {
			logger.error("Exception accessing the GeoCode url : " + geoCodeApi + ", " + indexable, e);
		}
		return null;
	}

	protected String getUri(String address) {
		StringBuilder builder = new StringBuilder();
		builder.append(geoCodeApi);
		builder.append("?");
		builder.append(ADDRESS);
		builder.append("=");
		builder.append(address);
		builder.append("&");
		builder.append(SENSOR);
		builder.append("=");
		builder.append("true");
		return builder.toString();
	}

	protected StringBuilder buildAddress(Indexable<?> indexable, StringBuilder builder) {
		if (indexable.isAddress()) {
			builder.append(indexable.getContent());
		}
		if (indexable.getChildren() != null) {
			for (Indexable<?> child : indexable.getChildren()) {
				buildAddress(child, builder);
			}
		}
		return builder;
	}

	public void setGeoCodeApi(String geoCodeApi) {
		this.geoCodeApi = geoCodeApi;
	}

}
