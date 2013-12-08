package ikube.model.geospatial;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import ikube.model.Persistable;

@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class GeoAltName extends Persistable {

	private int alternateNameId; // : the id of this alternate name, int
	private int geonameid; // : geonameId referring to id in table 'geoname', int
	@Column(length = 7)
	private String isolanguage; // : iso 639 language code 2- or 3-characters; 4-characters 'post' for postal codes and 'iata','icao' and
								// faac for airport codes, fr_1793 for French Revolution names, abbr for abbreviation, link for a website,
								// varchar(7)
	@Column(length = 200)
	private String alternateName; // : alternate name or name variant, varchar(200)
	private boolean isPreferredName; // : '1', if this alternate name is an official/preferred name
	private boolean isShortName; // : '1', if this is a short name like 'California' for 'State of California'

	public int getAlternateNameId() {
		return alternateNameId;
	}

	public void setAlternateNameId(int alternateNameId) {
		this.alternateNameId = alternateNameId;
	}

	public int getGeonameid() {
		return geonameid;
	}

	public void setGeonameid(int geonameid) {
		this.geonameid = geonameid;
	}

	public String getIsolanguage() {
		return isolanguage;
	}

	public void setIsolanguage(String isolanguage) {
		this.isolanguage = isolanguage;
	}

	public String getAlternateName() {
		return alternateName;
	}

	public void setAlternateName(String alternateName) {
		this.alternateName = alternateName;
	}

	public boolean getIsPreferredName() {
		return isPreferredName;
	}

	public void setIsPreferredName(boolean isPreferredName) {
		this.isPreferredName = isPreferredName;
	}

	public boolean getIsShortName() {
		return isShortName;
	}

	public void setIsShortName(boolean isShortName) {
		this.isShortName = isShortName;
	}

}
