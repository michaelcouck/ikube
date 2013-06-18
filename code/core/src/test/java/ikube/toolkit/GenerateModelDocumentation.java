package ikube.toolkit;

import ikube.model.Attribute;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableDataSource;
import ikube.model.IndexableDictionary;
import ikube.model.IndexableEmail;
import ikube.model.IndexableFileSystem;
import ikube.model.IndexableFileSystemCsv;
import ikube.model.IndexableFileSystemLog;
import ikube.model.IndexableFileSystemWiki;
import ikube.model.IndexableInternet;
import ikube.model.IndexableTable;

import java.lang.reflect.Field;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.springframework.util.ReflectionUtils;

/**
 * This class will just look through the entities for the annotation {@link Attribute} then extract the information from each fieldin the
 * entity and produce an html table of the indexables, the fields, and the description of the fields.
 * 
 * @author Michael Couck
 * @since 19.02.13
 * @version 01.00
 */
public class GenerateModelDocumentation {

	private static final Class<?>[] CLASSES = new Class<?>[] { Indexable.class, IndexableFileSystemLog.class, IndexableEmail.class,
			IndexableFileSystem.class, IndexableFileSystemCsv.class, IndexableInternet.class, IndexableTable.class, IndexableColumn.class,
			IndexableDataSource.class, IndexableFileSystemWiki.class, IndexableDictionary.class };
	
	public static void main(String[] args) {
		new GenerateModelDocumentation().createEntityFieldTable();
	}
	
	public String createEntityFieldTable() {
		Document document = DocumentFactory.getInstance().createDocument();
		Element tableElement = document.addElement("table");
		Element headerRowElement = tableElement.addElement("tr");
		XmlUtilities.addElement(headerRowElement, "th", "Name");
		XmlUtilities.addElement(headerRowElement, "th", "Property");
		XmlUtilities.addElement(headerRowElement, "th", "Lucene field");
		XmlUtilities.addElement(headerRowElement, "th", "Description");
		for (final Class<?> klass : CLASSES) {
			createEntityTableRow(klass, tableElement);
		}
		return document.asXML().replaceAll("zzz", "<br>");
	}

	private void createEntityTableRow(final Class<?> klass, final Element tableElement) {
		final Element rowElement = tableElement.addElement("tr");
		XmlUtilities.addElement(rowElement, "td", klass.getSimpleName());
		final StringBuilder propertyBuilder = new  StringBuilder();
		final StringBuilder luceneFieldBuilder = new  StringBuilder();
		final StringBuilder descriptionBuilder = new  StringBuilder();
		class ModelAttributeFieldCallback implements ReflectionUtils.FieldCallback {
			@Override
			public void doWith(final Field field) throws IllegalArgumentException, IllegalAccessException {
				Attribute attribute = field.getAnnotation(Attribute.class);
				propertyBuilder.append(field.getName() + "zzz");
				luceneFieldBuilder.append(attribute.field() + "zzz");
				descriptionBuilder.append(attribute.description() + "zzz");
			}
		}
		class ModelAttributeFieldFilter implements ReflectionUtils.FieldFilter {
			@Override
			public boolean matches(final Field field) {
				return field.getAnnotation(Attribute.class) != null && !field.getName().equals("id");
			}
		}
		ReflectionUtils.doWithFields(klass, new ModelAttributeFieldCallback(), new ModelAttributeFieldFilter());
		XmlUtilities.addElement(rowElement, "td", propertyBuilder.toString());
		XmlUtilities.addElement(rowElement, "td", luceneFieldBuilder.toString());
		Element descriptionElement = XmlUtilities.addElement(rowElement, "td", descriptionBuilder.toString());
		descriptionElement.addAttribute("nowrap", "nowrap");
	}

}