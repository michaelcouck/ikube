package ikube.experimental;

import ikube.action.index.handler.strategy.geocode.Coordinate;

import org.apache.lucene.index.IndexWriter;

public class Examples {

	public static final Coordinate EBIKON_6030_6031 = new Coordinate(47.0819237, 8.3415740, "Ebikon");
	public static final Coordinate SEEBACH_8052 = new Coordinate(47.4232860, 8.5422655, "Seebach");
	public static final Coordinate ZUERICH_8000 = new Coordinate(47.3690239, 8.5380326, "Zürich in 8000");
	public static final Coordinate SCHWAMMENDINGEN_8051 = new Coordinate(47.4008593, 8.5781373, "Schwammedingen");
	public static final Coordinate ADLISWIL_8134 = new Coordinate(47.3119892, 8.5256064, "Adliswil");
	public static final Coordinate KNONAU_8934 = new Coordinate(47.2237640, 8.4611790, "Knonau");
	public static final Coordinate BAAR_6341 = new Coordinate(47.1934110, 8.5230670, "Baar");
	

	public static void createExampleLocations(final IndexWriter indexWriter) throws Exception {
		final SpatialHelper spatialHelper = new SpatialHelper(20.0d, 10.0d);
		spatialHelper.addLoc(indexWriter, EBIKON_6030_6031.getName(), EBIKON_6030_6031);
		spatialHelper.addLoc(indexWriter, SEEBACH_8052.getName(), SEEBACH_8052);
		spatialHelper.addLoc(indexWriter, ZUERICH_8000.getName(), ZUERICH_8000);
		spatialHelper.addLoc(indexWriter, SCHWAMMENDINGEN_8051.getName(), SCHWAMMENDINGEN_8051);
		spatialHelper.addLoc(indexWriter, ADLISWIL_8134.getName(), ADLISWIL_8134);
		spatialHelper.addLoc(indexWriter, KNONAU_8934.getName(), KNONAU_8934);
		spatialHelper.addLoc(indexWriter, BAAR_6341.getName(), BAAR_6341);
		for (int i = 0; i < 10000; i++) {
			// spatialHelper.addLoc(indexWriter, "Zürich_" + i, new Coordinate(ZUERICH_8000.getLat() + i / 100000D, ZUERICH_8000.getLon() + i / 100000D));
		}
	}
}
