package ikube.model.geospatial;

import ikube.model.Coordinate;
import ikube.model.Persistable;
import org.apache.openjpa.persistence.jdbc.Index;

import javax.persistence.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 08-12-2013
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NamedQueries(value = {@NamedQuery(name = GeoCity.DELETE_ALL, query = GeoCity.DELETE_ALL)})
public class GeoCity extends Persistable {

    public static final String DELETE_ALL = "delete from GeoCity g";

    @Index(unique = true, enabled = true, name = "city_name_index", specified = true)
    private String name;
    private Coordinate coordinate;

    // @PrimaryKeyJoinColumn
    @ManyToOne(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    private GeoCountry parent;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public GeoCountry getParent() {
        return parent;
    }

    public void setParent(GeoCountry parent) {
        this.parent = parent;
    }

}
