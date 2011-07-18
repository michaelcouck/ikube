package ikube.index.spatial.geocode;

import ikube.index.spatial.Coordinate;

/**
 * This class will search the geospatial index for the target address. If there are results then there will be a latitude and longitude for
 * the address. These co-ordinates will be used to create a Coordinate object which can be used to enrich the indexes with geospatial data.
 * 
 * @author Michael Couck
 * @since 06.03.11
 * @version 01.00
 */
public interface IGeocoder {

	/**
	 * This method will return a co-ordinate object based on the address. The GeoCoder(whichever one, either the Ikube geocoder or the
	 * Google geocoder) must be able to take the address, find the best match co-ordinates for the address and return the co-ordinate object
	 * for that address.
	 * 
	 * @param address
	 *            the address that we need the co-ordinate latitude and longitude for
	 * @return the co-ordinate for the address in the parameter list
	 */
	Coordinate getCoordinate(String address);

	/**
	 * Sets the url where the GeoCoder will go to get the co-ordinates for the address being searched.
	 * 
	 * @param searchUrl
	 *            the url for the geospatial data results, could be the Rest API from Google or the web service from Ikube
	 */
	void setSearchUrl(String searchUrl);

}
