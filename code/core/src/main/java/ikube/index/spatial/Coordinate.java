package ikube.index.spatial;

public class Coordinate {

	private transient final double lat;
	private transient final double lon;
	private transient String name;

	public Coordinate(final double lat, final double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	public Coordinate(final double lat, final double lon, final String name) {
		this(lat, lon);
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
}
