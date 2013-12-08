package ikube.model.geospatial;

import ikube.model.Persistable;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class GeoZone extends Persistable {

	private double dst;
	private double gmt;
	private String timeZone;
	private double rawOffset;
	private String countryCode;

	public double getDst() {
		return dst;
	}

	public void setDst(double dst) {
		this.dst = dst;
	}

	public double getGmt() {
		return gmt;
	}

	public void setGmt(double gmt) {
		this.gmt = gmt;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public double getRawOffset() {
		return rawOffset;
	}

	public void setRawOffset(double rawOffset) {
		this.rawOffset = rawOffset;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

}
