package ikube.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.persistence.*;

/**
 * This object represents an address and the latitude and longitude co-ordinates for that address.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 25-02-2011
 */
@Entity
@Embeddable
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Coordinate extends Persistable {

    @Column
    private double latitude;
    @Column
    private double longitude;
    @Column
    private String name;

    public Coordinate() {
    }

    public Coordinate(final double lat, final double lon) {
        this(lat, lon, null);
    }

    public Coordinate(final double lat, final double lon, final String name) {
        this.latitude = lat;
        this.longitude = lon;
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double lat) {
        this.latitude = lat;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double lon) {
        this.longitude = lon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
