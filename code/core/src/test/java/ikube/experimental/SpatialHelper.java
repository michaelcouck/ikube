package ikube.experimental;

import ikube.action.index.handler.enrich.geocode.Coordinate;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.spatial.tier.projections.CartesianTierPlotter;
import org.apache.lucene.spatial.tier.projections.IProjector;
import org.apache.lucene.spatial.tier.projections.SinusoidalProjector;
import org.apache.lucene.util.NumericUtils;

public class SpatialHelper {

	private final IProjector projector;
	private final int startTier;
	private final int endTier;

	public static final double MILE = 1.609344;
	public static String LAT_FIELD = "lat";
	public static String LON_FIELD = "lng";

	public SpatialHelper(final double minKm, final double maxKm) {
		projector = new SinusoidalProjector();
		CartesianTierPlotter ctp = new CartesianTierPlotter(0, projector, CartesianTierPlotter.DEFALT_FIELD_PREFIX);
		startTier = ctp.bestFit(getMiles(minKm));
		endTier = ctp.bestFit(getMiles(maxKm));
	}

	public void addLoc(final IndexWriter writer, final String name, final Coordinate coord) throws Exception {
		final Document doc = new Document();
		doc.add(new Field("name", name, Field.Store.YES, Index.ANALYZED));
		addSpatialLcnFields(coord, doc);
		writer.addDocument(doc);
	}

	public static double getMiles(final double km) {
		return km / MILE;
	}

	public static double getKm(final double miles) {
		return miles * MILE;
	}

	private void addSpatialLcnFields(final Coordinate coord, final Document document) {
		document.add(new Field("lat", NumericUtils.doubleToPrefixCoded(coord.getLat()), Field.Store.YES, Field.Index.NOT_ANALYZED));
		document.add(new Field("lng", NumericUtils.doubleToPrefixCoded(coord.getLon()), Field.Store.YES, Field.Index.NOT_ANALYZED));
		addCartesianTiers(coord, document);
	}

	private void addCartesianTiers(final Coordinate coord, final Document document) {
		for (int tier = startTier; tier <= endTier; tier++) {
			CartesianTierPlotter ctp = new CartesianTierPlotter(tier, projector, CartesianTierPlotter.DEFALT_FIELD_PREFIX);
			final double boxId = ctp.getTierBoxId(coord.getLat(), coord.getLon());
			document.add(new Field(ctp.getTierFieldName(), NumericUtils.doubleToPrefixCoded(boxId), Field.Store.YES,
					Field.Index.NOT_ANALYZED_NO_NORMS));
		}
	}
}
