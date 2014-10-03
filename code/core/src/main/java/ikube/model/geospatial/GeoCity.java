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
public class GeoCity extends Persistable {

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
