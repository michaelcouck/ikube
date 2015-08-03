package ikube.model.geospatial;

import ikube.model.Persistable;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 10-29-2011
 */
@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class GeoName extends Persistable {

    @Column
    private int geonameid; // : integer id of record in geonames database
    @Column(length = 200)
    private String name; // : name of geographical point (utf8) varchar(200)
    @Column(length = 200)
    private String city;
    @Column(length = 200)
    private String country;
    @Column(length = 200)
    private String asciiname; // : name of geographical point in plain ascii characters, varchar(200)
    @Lob
    @Column(length = 5000)
    @Basic(fetch = FetchType.EAGER)
    private String alternatenames; // : alternate names, comma separated varchar(5000)
    @Column
    private double latitude; // : latitude in decimal degrees (wgs84)
    @Column
    private double longitude; // : longitude in decimal degrees (wgs84)
    @Column(length = 1)
    private String featureClass; // : see http://www.geonames.org/export/codes.html, char(1)
    @Column(length = 10)
    private String featureCode; // : see http://www.geonames.org/export/codes.html, varchar(10)
    @Column(length = 2)
    private String countryCode; // : ISO-3166 2-letter country code, 2 characters
    @Column(length = 60)
    private String cc2; // : alternate country codes, comma separated, ISO-3166 2-letter country code, 60 characters
    @Column(length = 20)
    private String admin1Code; // : fipscode (subject to change to iso code), see exceptions below, see file admin1Codes.txt for display
    // names of this code; varchar(20)
    @Column(length = 80)
    private String admin2Code; // : code for the second administrative division, a county in the US, see file admin2Codes.txt; varchar(80)
    @Column(length = 20)
    private String admin3Code; // : code for third level administrative division, varchar(20)
    @Column(length = 20)
    private String admin4Code; // : code for fourth level administrative division, varchar(20)
    @Column
    private int population = 0; // : bigint (8 byte int)
    @Column
    private int elevation; // : in meters, integer
    @Column
    private int gtopo30; // : average elevation of 30'x30' (ca 900mx900m) area in meters, integer
    @Column(length = 48)
    private String timezone; // : the timezone id (see file timeZone.txt)
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date modification; // date : date of last modification in yyyy-MM-dd format

    @Version
    private long version;

    public int getGeonameid() {
        return geonameid;
    }

    public void setGeonameid(final int geonameid) {
        this.geonameid = geonameid;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(final String country) {
        this.country = country;
    }

    public String getAsciiname() {
        return asciiname;
    }

    public void setAsciiname(final String asciiname) {
        this.asciiname = asciiname;
    }

    public String getAlternatenames() {
        return alternatenames;
    }

    public void setAlternatenames(final String alternatenames) {
        this.alternatenames = alternatenames;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(final double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(final double longitude) {
        this.longitude = longitude;
    }

    public String getFeatureClass() {
        return featureClass;
    }

    public void setFeatureClass(final String featureClass) {
        this.featureClass = featureClass;
    }

    public String getFeatureCode() {
        return featureCode;
    }

    public void setFeatureCode(final String featureCode) {
        this.featureCode = featureCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(final String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCc2() {
        return cc2;
    }

    public void setCc2(final String cc2) {
        this.cc2 = cc2;
    }

    public String getAdmin1Code() {
        return admin1Code;
    }

    public void setAdmin1Code(final String admin1Code) {
        this.admin1Code = admin1Code;
    }

    public String getAdmin2Code() {
        return admin2Code;
    }

    public void setAdmin2Code(final String admin2Code) {
        this.admin2Code = admin2Code;
    }

    public String getAdmin3Code() {
        return admin3Code;
    }

    public void setAdmin3Code(final String admin3Code) {
        this.admin3Code = admin3Code;
    }

    public String getAdmin4Code() {
        return admin4Code;
    }

    public void setAdmin4Code(final String admin4Code) {
        this.admin4Code = admin4Code;
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(final int population) {
        this.population = population;
    }

    public int getElevation() {
        return elevation;
    }

    public void setElevation(final int elevation) {
        this.elevation = elevation;
    }

    public int getGtopo30() {
        return gtopo30;
    }

    public void setGtopo30(final int gtopo30) {
        this.gtopo30 = gtopo30;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(final String zone) {
        this.timezone = zone;
    }

    public Date getModification() {
        return modification;
    }

    public void setModification(final Date modification) {
        this.modification = modification;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(final long version) {
        this.version = version;
    }
}
