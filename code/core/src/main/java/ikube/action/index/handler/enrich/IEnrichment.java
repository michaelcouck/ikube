package ikube.action.index.handler.enrich;

import ikube.action.index.handler.enrich.geocode.Coordinate;
import ikube.model.Indexable;

import org.apache.lucene.document.Document;

/**
 * This class will add spatial fields to the index based on either the latitude and longitude defined in one of the columns that are
 * indexables or it will go to the spatial web service with the address in the table and search for the location, i.e. the latitude and
 * longitude for the address and use the first result that comes back.
 * 
 * @author Michael Couck
 * @since 12.04.11
 * @version 01.00
 */
public interface IEnrichment {

	/**
	 * This method will get the co-ordinates for the indexable based on the names of the children. For example if there is a table with
	 * latitude and longitude columns in it, and the fields are defined in the configuration as 'latitude' and 'longitude' respectively then
	 * these fields will be found, and a Coordinate object created and returned.
	 * 
	 * @param indexable
	 *            the indexable that could have children that are the co-ordinates for the record
	 * @return
	 */
	public Coordinate getCoordinate(Indexable<?> indexable);

	/**
	 * This method adds two fields to the index for the latitude and longitude.
	 * 
	 * @param coordinate
	 *            the co-ordinate of the address/record
	 * @param document
	 *            the document to add the latitude and longitude to
	 */
	public void addSpatialLocationFields(final Coordinate coordinate, final Document document);

	/**
	 * This method adds the Cartesian tiers to the index. Essentially what this means is that the geo-space will be broken into blocks. The
	 * co-ordinate will be used to place the object in one of these blocks.
	 * 
	 * @param coordinate
	 *            the co-ordinate of the target object
	 * @param document
	 *            the document to add the tiers to
	 * @param startTier
	 *            the start tier
	 * @param endTier
	 *            and the end tier
	 */
	public void addCartesianTiers(final Coordinate coordinate, final Document document, final int startTier, final int endTier);

	/**
	 * This method will build the address from the indexable. The indexable will be recursively iterated to find all the child indexables
	 * that are flagged as address components. For example a table indexable could have columns for street, city and country as address
	 * components.
	 * 
	 * @param indexable
	 *            the indexable to build the address from
	 * @param builder
	 *            the builder to add the address components to
	 * @return the builder with all the available address components
	 */
	public StringBuilder buildAddress(final Indexable<?> indexable, final StringBuilder builder);

	/** Getters and setters. */

	public void setMinKm(final double minKm);

	public void setMaxKm(final double maxKm);
}
