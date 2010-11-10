package ikube.index.mock;

import java.util.HashMap;
import java.util.Map;

import mockit.MockClass;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;

@MockClass(realClass = Document.class)
public class DocumentMock {

	private Map<String, Fieldable> fields;

	public DocumentMock() {
		this.fields = new HashMap<String, Fieldable>();
	}

	public void add(Fieldable field) {
		fields.put(field.name(), field);
	}

	public Field getField(String name) {
		return (Field) fields.get(name);
	}

}
