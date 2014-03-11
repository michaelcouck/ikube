package ikube.action.index.handler.strategy;

import au.com.bytecode.opencsv.CSVReader;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Shape;
import ikube.IConstants;
import ikube.action.index.handler.IStrategy;
import ikube.action.index.handler.strategy.geocode.IGeocoder;
import ikube.database.IDataBase;
import ikube.model.Coordinate;
import ikube.model.geospatial.GeoCity;
import ikube.model.geospatial.GeoCountry;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.HashUtilities;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

/**
 * This is the base class for strategies that add geospatial fields to the index.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 18-12-2013
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public abstract class AGeospatialEnrichmentStrategy extends AStrategy {

    protected static Map<Long, GeoCity> GEO_CITY;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${max.geohash.levels}")
    protected int maxGeohashLevels = IConstants.MAX_GEOHASH_LEVELS;
    protected String countryCityFile = "country-city-language-coordinate.properties";

    /**
     * The geocoder to get the co-ordinates for the indexable.
     */
    @Autowired
    protected IGeocoder geocoder;
    @Autowired
    private IDataBase dataBase;

    SpatialContext spatialContext;
    SpatialStrategy spatialStrategy;

    public AGeospatialEnrichmentStrategy() {
        this(null);
    }

    public AGeospatialEnrichmentStrategy(final IStrategy nextStrategy) {
        super(nextStrategy);
    }

    @SuppressWarnings("deprecation")
    public final void addSpatialLocationFields(final Coordinate coordinate, final Document document) {
        // Note to self: This takes an x and y co-ordinate so the
        // order must be longitude(x) and latitude(y), not the other way
        Shape shape = spatialContext.makePoint(coordinate.getLongitude(), coordinate.getLatitude());
        for (IndexableField indexableField : spatialStrategy.createIndexableFields(shape)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Adding spatial field : " + indexableField);
            }
            document.add(indexableField);
        }
        // Store this field any way
        document.add(new StoredField(spatialStrategy.getFieldName(), spatialContext.toString(shape)));
    }

    @Override
    public void initialize() {
        spatialContext = SpatialContext.GEO;
        SpatialPrefixTree spatialPrefixTree = new GeohashPrefixTree(spatialContext, maxGeohashLevels);
        this.spatialStrategy = new RecursivePrefixTreeStrategy(spatialPrefixTree, IConstants.POSITION_FIELD_NAME);

        if (GEO_CITY == null) {
            File baseDirectory = new File(IConstants.IKUBE_DIRECTORY);
            File file = FileUtilities.findFileRecursively(baseDirectory, countryCityFile);
            if (file != null) {
                loadCountries(file);
            }

            GEO_CITY = new HashMap<>();
            Collection<GeoCity> geoCities = dataBase.find(GeoCity.class, 0, Integer.MAX_VALUE);
            if (geoCities != null) {
                for (final GeoCity geoCity : geoCities) {
                    Long hash = HashUtilities.hash(geoCity.getName());
                    GEO_CITY.put(hash, geoCity);
                }
                logger.info("Loaded country/city map : " + GEO_CITY.size());
            }
        }
    }

    /**
     * This method will take the country/city/language/co-ordinate file (a csv file), with the countries,
     * their capital city, the primary language spoken in the country/city and the co-ordinate of the city
     * and load them into the database.
     *
     * @param file the file to load the countries from, and cities
     */
    private void loadCountries(final File file) {
        int removed = dataBase.remove(GeoCountry.DELETE_ALL);
        logger.info("Removed countries : " + removed);
        Reader reader = null;
        CSVReader csvReader = null;
        try {
            reader = new FileReader(file);
            csvReader = new CSVReader(reader, '|');
            List<String[]> data = csvReader.readAll();
            for (final String[] datum : data) {
                double latitude = Double.parseDouble(datum[3]);
                double longitude = Double.parseDouble(datum[4]);
                Coordinate coordinate = new Coordinate(latitude, longitude);

                GeoCity geoCity = new GeoCity();
                GeoCountry geoCountry = new GeoCountry();

                // Setting this here affects OpenJpa for some reason! WTF!?
                // geoCity.setName(datum[1]);
                geoCity.setCoordinate(coordinate);
                geoCity.setParent(geoCountry);

                geoCountry.setName(datum[0]);
                geoCountry.setLanguage(datum[2]);
                geoCountry.setChildren(Arrays.asList(geoCity));

                dataBase.persist(geoCountry);
                geoCity.setName(datum[1]);
                dataBase.merge(geoCity);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(csvReader);
        }
    }

}