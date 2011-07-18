package ikube.index.spatial;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * This object represents an address and the latitude and longitude co-ordinates for that address.
 * 
 * @author Michael Couck
 * @since 25.02.11
 * @version 01.00
 */
public class Coordinate {

	private final double lat;
	private final double lon;
	private final String name;

	public Coordinate(final double lat, final double lon) {
		this(lat, lon, null);
	}

	public Coordinate(final double lat, final double lon, final String name) {
		this.lat = lat;
		this.lon = lon;
		this.name = name;
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
