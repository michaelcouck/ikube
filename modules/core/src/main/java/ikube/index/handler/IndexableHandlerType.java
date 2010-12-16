package ikube.index.handler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is a marker annotation to configure indexables against handlers. This annotation is to be added to methods that can possibly handle
 * indexables.
 * 
 * @author Michael Couck
 * @since 11.11.10
 * @version 01.00
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IndexableHandlerType {

	/**
	 * @return the type of indexable that this handler can handle
	 */
	public Class<?> type();

}
