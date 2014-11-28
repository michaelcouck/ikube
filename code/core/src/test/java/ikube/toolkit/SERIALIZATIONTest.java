package ikube.toolkit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.model.IndexableTable;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

/**
 * This test is just to see that the serializer is correctly encoding the languages.
 * 
 * @author Michael Couck
 * @since 21.05.2011
 * @version 01.00
 */
public class SERIALIZATIONTest extends AbstractTest {

	private String russian = "Что определяет производительность";
	private String arabic = "تيات تبحث عن ابن الحلالللصداقة و الزوا";

	@Test
	public void serialize() {
		ArrayList<HashMap<String, String>> results = getResults();
		String xml = SERIALIZATION.serialize(results);
		logger.info("Results : " + xml);
		assertTrue("The serialized string should contain the Russian characters : ", xml.contains(russian));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void deserialize() {
		ArrayList<HashMap<String, String>> results = getResults();
		String xml = SERIALIZATION.serialize(results);
		logger.info("Xml : " + xml);
		results = (ArrayList<HashMap<String, String>>) SERIALIZATION.deserialize(xml);
		String fragment = results.get(0).get(IConstants.FRAGMENT);
		assertTrue("The de-serialized fragment should contain the Russian characters : ", fragment.contains(russian));
	}

	private ArrayList<HashMap<String, String>> getResults() {
		ArrayList<HashMap<String, String>> results = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> result = new HashMap<String, String>();
		result.put(IConstants.FRAGMENT, russian + arabic);
		results.add(result);
		return results;
	}

	@Test
	public void setTransientFields() throws Exception {
		SERIALIZATION.setTransientFields(IndexContext.class, new ArrayList<Class<?>>());
		BeanInfo info = Introspector.getBeanInfo(IndexContext.class);
		PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
		boolean containsIndex = Boolean.FALSE;
		for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
			String name = propertyDescriptor.getName();
			if (name != null && name.equals("indexWriters")) {
				containsIndex = Boolean.TRUE;
			}
		}
		assertTrue("The index field should be set to transient : ", containsIndex);
	}

	@Test
	public void cloneIndexableTable() throws Exception {
		IndexableTable indexableTable = new IndexableTable();
		Object clone = SERIALIZATION.clone(indexableTable);
		assertNotNull(clone);
	}

}