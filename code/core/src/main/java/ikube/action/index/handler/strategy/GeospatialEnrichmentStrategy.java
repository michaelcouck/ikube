package ikube.action.index.handler.strategy;

import ikube.IConstants;
import ikube.action.index.handler.IStrategy;
import ikube.model.Coordinate;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;

import java.util.regex.Pattern;

/**
 * This strategy will intercept typically databases that have co-ordinate data, geo co-ordinates. It will
 * then add geo-hash fields to the documents in the Lucene index that can then be queried and the results can
 * be sorted by distance. Another application of course is that the query can limit all results from a
 * specific point, i.e. only places that have 'Julia Roberts' but limited to Hollywood Boulevard.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 20-01-2012
 */
public final class GeospatialEnrichmentStrategy extends AGeospatialEnrichmentStrategy {

    private Pattern latPattern;
    private Pattern lngPattern;

    public GeospatialEnrichmentStrategy() {
        this(null);
    }

    public GeospatialEnrichmentStrategy(final IStrategy nextStrategy) {
        super(nextStrategy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean aroundProcess(final IndexContext indexContext, final Indexable indexable, final Document document, final Object resource) throws Exception {
        // The parameters can be either the columns and values from a csv file
        // or the columns from a table filled in with the values. All the logic from the Enrichment class can
        // be used in here to keep all the enrichment logic in the same place
        Coordinate coordinate = getCoordinate(indexable);
        if (coordinate != null) {
            addSpatialLocationFields(coordinate, document);
        }
        return super.aroundProcess(indexContext, indexable, document, resource);
    }

    /**
     * This method will look through the children of the indexable, and try to find the latitude and longitude to
     * generate a co-ordinate. If there is no c-ordinate data in the indexable hierarchy, then this method will potentially
     * go to a reverse geo-coding web service to get the co-ordinate based on the name, as a last resort.
     *
     * @param indexable the indexable to try to find ge-spatial data in
     * @return the co-ordinate generated from the latitude and longitude in the indexable,
     * or null if there is no geo-data in the indexable, and the reverse
     * geo-coder didn't match anything either
     */
    final Coordinate getCoordinate(final Indexable indexable) {
        Double latitude = null;
        Double longitude = null;
        if (indexable.getChildren() != null) {
            for (final Indexable child : indexable.getChildren()) {
                if (IndexableColumn.class.isAssignableFrom(child.getClass())) {
                    IndexableColumn indexableColumn = (IndexableColumn) child;
                    Object content = indexableColumn.getContent();
                    if (content == null || indexableColumn.getFieldName() == null) {
                        continue;
                    }
                    String fieldName = indexableColumn.getFieldName().toLowerCase();
                    if (latPattern.matcher(fieldName).matches()) {
                        latitude = Double.parseDouble(content.toString());
                    } else if (lngPattern.matcher(fieldName).matches()) {
                        longitude = Double.parseDouble(content.toString());
                    }
                }
            }
        }
        Coordinate coordinate = null;
        if (latitude != null && longitude != null) {
            coordinate = new Coordinate(latitude, longitude);
        } else {
            String address = buildAddress(indexable, new StringBuilder()).toString();
            // The GeoCoder is a last resort in fact, this will hurt!
            if (geocoder != null) {
                coordinate = geocoder.getCoordinate(address);
            }
        }
        logger.info("Coordinate : " + coordinate);
        return coordinate;
    }

    final StringBuilder buildAddress(final Indexable indexable, final StringBuilder builder) {
        if (indexable.isAddress()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            String content = indexable.getAddressContent();
            if (!StringUtils.isEmpty(content)) {
                builder.append(content);
            }
        }
        if (indexable.getChildren() != null) {
            for (final Indexable child : indexable.getChildren()) {
                buildAddress(child, builder);
            }
        }
        return builder;
    }

    public void initialize() {
        super.initialize();
        StringBuilder builder = new StringBuilder();
        builder.append(".*(");
        builder.append(IConstants.LATITUDE);
        builder.append(").*");
        latPattern = Pattern.compile(builder.toString());

        builder = new StringBuilder();
        builder.append(".*(");
        builder.append(IConstants.LONGITUDE);
        builder.append(").*");
        lngPattern = Pattern.compile(builder.toString());
    }

}