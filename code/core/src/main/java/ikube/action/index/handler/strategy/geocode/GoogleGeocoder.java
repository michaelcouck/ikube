package ikube.action.index.handler.strategy.geocode;

import ikube.IConstants;
import ikube.model.Coordinate;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.XmlUtilities;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

/**
 * This is the implementation for the Google GeoCoder API. There can only be 2500 searches using this API, and the results have to be used
 * to populate a GoogleMap in fact, unless you have a premium account, which is what this class is for finally, which will be expensive in
 * fact, and it could be interesting to implement a batch GeoCoder in this case.
 * 
 * @author Michael Couck
 * @since 06.03.11
 * @version 01.00
 */
public class GoogleGeocoder implements IGeocoder {

	/** Geocode API and parameters. */
	protected static final String ADDRESS = "address";
	protected static final String SENSOR = "sensor";

	private static final Logger LOGGER = Logger.getLogger(GoogleGeocoder.class);

	private transient String searchUrl;

	@Override
	public Coordinate getCoordinate(final String address) {
		String strippedAddress = null;
		try {
			strippedAddress = StringUtils.trim(address);
			strippedAddress = URLEncoder.encode(strippedAddress, IConstants.ENCODING);
			// Call the geocoder with the address
			String uri = getUri(strippedAddress);
			URL url = new URL(uri);
			String xml = FileUtilities.getContents(url.openStream(), Integer.MAX_VALUE).toString();
			InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
			Element rootElement = XmlUtilities.getDocument(inputStream, IConstants.ENCODING).getRootElement();
			Element element = XmlUtilities.getElement(rootElement, IConstants.LOCATION);
			Element latitudeElement = XmlUtilities.getElement(element, IConstants.LAT);
			Element longitudeElement = XmlUtilities.getElement(element, IConstants.LNG);
			double lat = Double.parseDouble(latitudeElement.getText());
			double lng = Double.parseDouble(longitudeElement.getText());
			return new Coordinate(lat, lng, strippedAddress);
		} catch (Exception e) {
			LOGGER.error("Exception accessing the GeoCode url : " + searchUrl + ", " + strippedAddress, e);
		}
		return null;
	}

	protected String getUri(final String address) {
        return searchUrl + "?" + ADDRESS + "=" + address + "&" + SENSOR + "=" + "true";
	}

	public void setSearchUrl(String searchUrl) {
		this.searchUrl = searchUrl;
	}

}
