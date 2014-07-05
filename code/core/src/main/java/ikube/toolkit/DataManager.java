package ikube.toolkit;

import au.com.bytecode.opencsv.CSVReader;
import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.model.Coordinate;
import ikube.model.geospatial.GeoCity;
import ikube.model.geospatial.GeoCountry;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 06-04-2014
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public class DataManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataManager.class);

    protected static String countryCityFile = "country-city-language-coordinate.properties";

    @Autowired
    private IDataBase dataBase;

    public void loadData() {
        loadCountries();
    }

    /**
     * This method will take the country/city/language/co-ordinate file (a csv file), with the countries,
     * their capital city, the primary language spoken in the country/city and the co-ordinate of the city
     * and load them into the database.
     */
    private void loadCountries() {
        File baseDirectory = new File(IConstants.IKUBE_DIRECTORY);
        File file = FileUtilities.findFileRecursively(baseDirectory, countryCityFile);

        Reader reader = null;
        CSVReader csvReader = null;
        try {
            reader = new FileReader(file);
            csvReader = new CSVReader(reader, '|');
            List<String[]> data = csvReader.readAll();
            long count = dataBase.count(GeoCity.class);
            if (count < data.size()) {
                int removed = dataBase.remove(GeoCountry.DELETE_ALL);
                LOGGER.info("Removed countries : " + removed);

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
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        } catch (final Exception e) {
            LOGGER.error("General exception loading the data : " + dataBase, e);
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(csvReader);
        }
    }

}
