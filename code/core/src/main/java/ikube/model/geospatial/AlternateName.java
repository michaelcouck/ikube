package ikube.model.geospatial;

import javax.persistence.Column;
import javax.persistence.Entity;

import ikube.model.Persistable;

@Entity()
public class AlternateName extends Persistable {

	private Integer alternateNameId; // : the id of this alternate name, int
	private Integer geonameid; // : geonameId referring to id in table 'geoname', int
	@Column(length = 7)
	private String isolanguage; // : iso 639 language code 2- or 3-characters; 4-characters 'post' for postal codes and 'iata','icao' and
								// faac for airport codes, fr_1793 for French Revolution names, abbr for abbreviation, link for a website,
								// varchar(7)
	@Column(length = 200)
	private String alternateName; // : alternate name or name variant, varchar(200)
	private Boolean isPreferredName; // : '1', if this alternate name is an official/preferred name
	private Boolean isShortName; // : '1', if this is a short name like 'California' for 'State of California'

	public Integer getAlternateNameId() {
		return alternateNameId;
	}

	public void setAlternateNameId(Integer alternateNameId) {
		this.alternateNameId = alternateNameId;
	}

	public Integer getGeonameid() {
		return geonameid;
	}

	public void setGeonameid(Integer geonameid) {
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

	public Boolean getIsPreferredName() {
		return isPreferredName;
	}

	public void setIsPreferredName(Boolean isPreferredName) {
		this.isPreferredName = isPreferredName;
	}

	public Boolean getIsShortName() {
		return isShortName;
	}

	public void setIsShortName(Boolean isShortName) {
		this.isShortName = isShortName;
	}

}
