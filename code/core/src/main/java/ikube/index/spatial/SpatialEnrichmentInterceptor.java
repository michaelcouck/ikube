package ikube.index.spatial;

import ikube.IConstants;
import ikube.model.Indexable;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.XmlUtilities;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.spatial.tier.projections.CartesianTierPlotter;
import org.apache.lucene.spatial.tier.projections.IProjector;
import org.apache.lucene.spatial.tier.projections.SinusoidalProjector;
import org.apache.lucene.util.NumericUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.dom4j.Element;

/**
 * @author Michael Couck
 * @since 06.03.2011
 * @version 01.00
 */
public class SpatialEnrichmentInterceptor implements ISpatialEnrichmentInterceptor {

	private Logger logger = Logger.getLogger(this.getClass());

	private int startTier;
	private int endTier;
	private IProjector projector = new SinusoidalProjector();

	@Override
	public Object enrich(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		// Iterate through all the indexable children of the indexable looking for address
		// fields. Concatenate them with a ',' in between. Call the Google geo coding API
		// for the latitude and longitude coordinates. Create the tiers for the location,
		// and add the resultant data to the document, simple.
		enrich(proceedingJoinPoint.getArgs());
		return proceedingJoinPoint.proceed();
	}

	protected void enrich(Object[] arguments) {
		IndexWriter indexWriter = null;
		Document document = null;
		Indexable<?> indexable = null;
		if (arguments != null) {
			for (Object argument : arguments) {
				if (argument == null) {
					continue;
				}
				Class<?> klass = argument.getClass();
				if (IndexWriter.class.isAssignableFrom(klass)) {
					indexWriter = (IndexWriter) argument;
				} else if (Document.class.isAssignableFrom(klass)) {
					document = (Document) argument;
				} else if (Indexable.class.isAssignableFrom(klass)) {
					indexable = (Indexable<?>) argument;
				}
			}
		}
		try {
			String address = buildAddress(indexable, new StringBuilder()).toString();
			// Call the Google geocoder with the address
			String uri = getUri(address);
			URL url = new URL(uri);
			String xml = FileUtilities.getContents(url.openStream(), Integer.MAX_VALUE).toString();
			InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
			Element rootElement = XmlUtilities.getDocument(inputStream, IConstants.ENCODING).getRootElement();
			Element element = XmlUtilities.getElement(rootElement, IConstants.LOCATION);
			Element latitudeElement = XmlUtilities.getElement(element, IConstants.LAT);
			Element longitudeElement = XmlUtilities.getElement(element, IConstants.LNG);
			double lat = Double.parseDouble(latitudeElement.getText());
			double lng = Double.parseDouble(longitudeElement.getText());
			Coordinate coordinate = new Coordinate(lat, lng, address);
			addLocation(indexWriter, document, address, coordinate);
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	protected void addLocation(final IndexWriter writer, final Document document, final String address, final Coordinate coordinate)
			throws Exception {
		document.add(new Field(IConstants.NAME, address, Field.Store.YES, Index.ANALYZED));
		addSpatialLocationFields(coordinate, document);
		writer.addDocument(document);
	}

	protected void addSpatialLocationFields(final Coordinate coordinate, final Document document) {
		document.add(new Field(IConstants.LAT, NumericUtils.doubleToPrefixCoded(coordinate.getLat()), Field.Store.YES,
				Field.Index.NOT_ANALYZED));
		document.add(new Field(IConstants.LNG, NumericUtils.doubleToPrefixCoded(coordinate.getLon()), Field.Store.YES,
				Field.Index.NOT_ANALYZED));
		addCartesianTiers(coordinate, document);
	}

	protected void addCartesianTiers(final Coordinate coordinate, final Document document) {
		for (int tier = startTier; tier <= endTier; tier++) {
			CartesianTierPlotter ctp = new CartesianTierPlotter(tier, projector, CartesianTierPlotter.DEFALT_FIELD_PREFIX);
			final double boxId = ctp.getTierBoxId(coordinate.getLat(), coordinate.getLon());
			document.add(new Field(ctp.getTierFieldName(), NumericUtils.doubleToPrefixCoded(boxId), Field.Store.YES,
					Field.Index.NOT_ANALYZED_NO_NORMS));
		}
	}

	protected String getUri(String address) {
		StringBuilder builder = new StringBuilder();
		builder.append(IConstants.GEO_CODE_API);
		builder.append("?");
		builder.append(IConstants.ADDRESS);
		builder.append("=");
		builder.append(address);
		builder.append("&");
		builder.append(IConstants.sensor);
		builder.append("=");
		builder.append("true");
		return builder.toString();
	}

	protected StringBuilder buildAddress(Indexable<?> indexable, StringBuilder builder) {
		if (indexable.isAddress()) {
			builder.append(indexable.getContent());
		}
		if (indexable.getChildren() != null) {
			for (Indexable<?> child : indexable.getChildren()) {
				buildAddress(child, builder);
			}
		}
		return builder;
	}

	public void setMinKm(double minKm) {
		CartesianTierPlotter ctp = new CartesianTierPlotter(0, projector, CartesianTierPlotter.DEFALT_FIELD_PREFIX);
		startTier = ctp.bestFit(minKm);
	}

	public void setMaxKm(double maxKm) {
		CartesianTierPlotter ctp = new CartesianTierPlotter(0, projector, CartesianTierPlotter.DEFALT_FIELD_PREFIX);
		endTier = ctp.bestFit(maxKm);
	}

}