package ikube.index.spatial.enrich;

import org.apache.lucene.document.Document;

import ikube.index.spatial.Coordinate;
import ikube.model.Indexable;

public interface IEnrichment {

	public int getMinKm(final double minKm);

	public int getMaxKm(final double maxKm);
	
	public void setMinKm(final double minKm);

	public void setMaxKm(final double maxKm);

	public Coordinate getCoordinate(Indexable<?> indexable);

	public void addSpatialLocationFields(final Coordinate coordinate, final Document document);

	public void addCartesianTiers(final Coordinate coordinate, final Document document, final int startTier, final int endTier);

	public StringBuilder buildAddress(final Indexable<?> indexable, final StringBuilder builder);

}
