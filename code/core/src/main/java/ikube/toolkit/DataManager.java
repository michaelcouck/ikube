package ikube.toolkit;

import au.com.bytecode.opencsv.CSVReader;
import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.model.Coordinate;
import ikube.model.geospatial.GeoCity;
import ikube.model.geospatial.GeoCountry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ikube.toolkit.FILE.findFileRecursively;
import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * This class is a utility class for any data loading that needs to be done in initialization. Like for
 * example inserting the countries and cities with their languages and co-ordinates into the database for the
 * Twitter geospatial lookup.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 06-04-2014
 */
public class DataManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataManager.class);

    @Value("${country-city-language-coordinate-file-name}")
    protected String countryCityFile = "cnt-cty-lng-co.properties";

    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private IDataBase dataBase;

    public void loadData() {
        loadCountries();
    }

    /**
     * This method will take the country/city/language/co-ordinate file (a csv file), with the countries,
     * their capital city, the primary language spoken in the country/city and the co-ordinate of the city
     * and load them into the database.
     */
    synchronized void loadCountries() {
        File baseDirectory = new File(IConstants.IKUBE_DIRECTORY);
        File file = findFileRecursively(baseDirectory, countryCityFile);

        Reader reader = null;
        CSVReader csvReader = null;
        try {
            if (file == null || !file.exists() || !file.isFile() || file.canRead() || dataBase == null) {
                LOGGER.warn("Not loading data for cities and countries : " + file + ", " + dataBase);
                return;
            }
            reader = new FileReader(file);
            csvReader = new CSVReader(reader, '|');
            List<String[]> data = csvReader.readAll();
            long count = dataBase.count(GeoCity.class);
            if (count < data.size()) {
                int removed = dataBase.remove(GeoCountry.DELETE_ALL);
                LOGGER.info("Removed countries : " + removed);

                List<GeoCountry> geoCountries = new ArrayList<>();
                List<GeoCity> geoCities = new ArrayList<>();

                for (final String[] datum : data) {
                    double latitude = Double.parseDouble(datum[3]);
                    double longitude = Double.parseDouble(datum[4]);
                    Coordinate coordinate = new Coordinate(latitude, longitude);

                    GeoCountry geoCountry = new GeoCountry();
                    GeoCity geoCity = new GeoCity();

                    geoCountry.setName(datum[0]);
                    geoCountry.setLanguage(datum[2]);

                    // Setting this here affects OpenJpa for some reason! WTF!?
                    // geoCity.setName(datum[1]);
                    geoCity.setCoordinate(coordinate);
                    geoCity.setParent(geoCountry);

                    geoCountry.setChildren(new ArrayList<>(Arrays.asList(geoCity)));

                    // dataBase.persist(geoCountry);
                    // geoCity.setName(datum[1]);
                    // dataBase.merge(geoCity);

                    geoCountries.add(geoCountry);
                    geoCities.add(geoCity);
                }

                dataBase.persistBatch(geoCountries);

                for (int i = 0; i < geoCities.size(); i++) {
                    GeoCity geoCity = geoCities.get(i);
                    geoCity.setName(data.get(i)[1]);
                }

                dataBase.mergeBatch(geoCities);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeQuietly(reader);
            closeQuietly(csvReader);
        }
    }

}
