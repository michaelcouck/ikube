package ikube.database.odb;

import java.util.List;

/**
 * This is a representation of an index in the Neodatis database. Although indexes are a pain in fact and cause more problems than they are
 * worth.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class Index {

	/** The class name of the index. */
	private String className;
	/** The fields in the class to create indexes on. */
	private List<String> fieldNames;

	public Index() {
	}

	public Index(String className, List<String> fieldNames) {
		setClassName(className);
		setFieldNames(fieldNames);
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public List<String> getFieldNames() {
		return fieldNames;
	}

	public void setFieldNames(List<String> fieldNames) {
		this.fieldNames = fieldNames;
	}

}
