package ikube.index.spatial.geocode;

import ikube.index.spatial.Coordinate;
import ikube.model.Indexable;

/**
 * @author Michael Couck
 * @since 06.03.11
 * @version 01.00
 */
public interface IGeocoder {

	Coordinate getCoordinate(Indexable<?> indexable);

}
