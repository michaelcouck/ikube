package ikube.action.index.handler.enrich.geocode;


import org.apache.log4j.Logger;

/**
 * This class is the implementation for the NavTec geocoder, but it is not completely implemented. Before you can hit the web service (which
 * is completely painful) you have to jump through rings on fire, and walk on broken glass...
 * 
 * @author Michael Couck
 * @since 06.03.11
 * @version 01.00
 */
public class NavtecGeocoder implements IGeocoder {

	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(this.getClass());

	@SuppressWarnings("unused")
	private String nameSpace = "need the name space for the service";
	@SuppressWarnings("unused")
	private String serviceName = "need the service name too... :)";
	@SuppressWarnings("unused")
	private String searchUrl = "http://maptp12.map24.com/map24/webservices1.5?soap=Map24Geocoder51";

	@Override
	public Coordinate getCoordinate(String address) {
		return null;
	}

	public void setSearchUrl(String searchUrl) {
		this.searchUrl = searchUrl;
	}

	public void setNameSpace(String nameSpace) {
		this.nameSpace = nameSpace;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

}
