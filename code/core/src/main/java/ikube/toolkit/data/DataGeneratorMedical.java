package ikube.toolkit.data;

import ikube.database.IDataBase;
import ikube.model.medical.Doctor;
import ikube.toolkit.XmlUtilities;

import java.io.InputStream;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

/**
 * This class generates data for the medical model.
 * 
 * @author Michael Couck
 * @since 23.04.2011
 * @version 01.00
 */
public class DataGeneratorMedical extends ADataGenerator {

	private InputStream inputStream;

	public DataGeneratorMedical(IDataBase dataBase) {
		super(dataBase);
	}

	@SuppressWarnings({ "unused", "unchecked" })
	public void generate() throws Exception {
		Document document = XmlUtilities.getDocument(inputStream, "UTF8");
		Element listingsElement = XmlUtilities.getElement(document.getRootElement(), "listings");
		List<Element> listingsElements = listingsElement.elements();
		logger.info("Results size : " + listingsElements.size());
		for (Element listingElement : listingsElements) {
			logger.debug("Listing : " + listingElement);
			Element latitudeElement = XmlUtilities.getElement(listingElement, "latitude");
			Element longitudeElement = XmlUtilities.getElement(listingElement, "longitude");
			Element streetAddressElement = XmlUtilities.getElement(listingElement, "streetAddress");
			Element cityElement = XmlUtilities.getElement(listingElement, "city");
			Element reportingLocationElement = XmlUtilities.getElement(listingElement, "reportingLocation");
			Element postCodeElement = XmlUtilities.getElement(listingElement, "zipCode");

			double latitude = Double.parseDouble(latitudeElement.getText());
			double longitude = Double.parseDouble(longitudeElement.getText());

			Doctor doctor = createInstance(Doctor.class);
			doctor.getAddress().setCountry("België");
			doctor.getAddress().setLatitude(latitude);
			doctor.getAddress().setLongitude(longitude);
			doctor.getAddress().setNumb(0);
			doctor.getAddress().setPostCode(postCodeElement.getText());
			doctor.getAddress().setProvince(reportingLocationElement.getText());
			doctor.getAddress().setStreet(streetAddressElement.getText());

			logger.debug("Inserting : " + doctor);
			dataBase.persist(doctor);
			entities.clear();
		}
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

}
