package ikube.web.tag;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;

import javax.servlet.jsp.tagext.Tag;

import org.junit.Test;

public class LinkTagTest extends ATagTest {

	@Test
	public void doEndTag() throws Exception {
		LinkTag linkTag = new LinkTag();
		PagerTag pagerTag = new PagerTag();
		pagerTag.setBodyContent(bodyContent);
		setField(pagerTag, "url", pagerTag.new Url("http://www.ikokoon.eu/ikokoon", 1));
		linkTag.setParent(pagerTag);
		int result = linkTag.doEndTag();
		assertEquals(Tag.EVAL_PAGE, result);
	}

	/**
	 * Sets the field in the object to the value specified in the parameter list.
	 *
	 * @param object
	 *            the target object to set the field for
	 * @param name
	 *            the name of the field
	 * @param value
	 *            the value to set for the field
	 * @return whether the field was set or not
	 */
	public static boolean setField(Object object, String name, Object value) {
		if (object == null) {
			return false;
		}
		Field field = getField(object.getClass(), name);
		if (field != null) {
			try {
				field.setAccessible(true);
				field.set(object, value);
				return true;
			} catch (Exception t) {
				return false;
			}
		}
		return false;
	}

	/**
	 * Gets a field in the class or in the hierarchy of the class.
	 *
	 * @param klass
	 *            the original class
	 * @param name
	 *            the name of the field
	 * @return the field in the object or super classes of the object
	 */
	public static Field getField(Class<?> klass, String name) {
		Field field = null;
		try {
			field = klass.getDeclaredField(name);
		} catch (Exception t) {
			t.getCause();
		}
		if (field == null) {
			Class<?> superClass = klass.getSuperclass();
			if (superClass != null) {
				field = getField(superClass, name);
			}
		}
		return field;
	}

}
