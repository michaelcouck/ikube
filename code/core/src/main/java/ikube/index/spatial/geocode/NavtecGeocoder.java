package ikube.index.spatial.geocode;

import ikube.index.spatial.Coordinate;
import ikube.service.ServiceLocator;

import org.apache.log4j.Logger;

/**
 * @author Michael Couck
 * @since 06.03.11
 * @version 01.00
 */
public class NavtecGeocoder implements IGeocoder {

	private Logger logger = Logger.getLogger(this.getClass());

	@Override
	public Coordinate getCoordinate(String address) {
		String url = "http://maptp12.map24.com/map24/webservices1.5?soap=Map24Geocoder51";
		String nameSpace = "";
		String serviceName = "";
		Object service = ServiceLocator.getService(Object.class, url, nameSpace, serviceName);
		logger.info("Service : " + service);
		return null;
	}

}
