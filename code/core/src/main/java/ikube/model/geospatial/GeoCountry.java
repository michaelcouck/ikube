package ikube.model.geospatial;

import ikube.model.Persistable;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

import org.apache.openjpa.persistence.jdbc.Index;

/**
 * @author Michael Couck
 * @since 08.12.13
 * @version 01.00
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NamedQueries(value = { @NamedQuery(name = GeoCountry.DELETE_ALL, query = GeoCountry.DELETE_ALL) })
public class GeoCountry extends Persistable {

	public static final String DELETE_ALL = "delete from GeoCountry g";

	@Column
	@Index(unique = true, enabled = true, name = "country_name_index", specified = true)
	private String name;
	private String language;

	// @PrimaryKeyJoinColumn
	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	private GeoZone geoZone;

	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "parent", fetch = FetchType.EAGER)
	private List<GeoCity> children;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public GeoZone getGeoZone() {
		return geoZone;
	}

	public void setGeoZone(GeoZone geoZone) {
		this.geoZone = geoZone;
	}

	public List<GeoCity> getChildren() {
		return children;
	}

	public void setChildren(List<GeoCity> children) {
		this.children = children;
	}

}
