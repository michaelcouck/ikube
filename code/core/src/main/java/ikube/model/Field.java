package ikube.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is somehow connected to the monitoring service. This is really not an elegant idea and should be completely removed and
 * replaced with something equally shit in Spring. 
 * 
 * @author Michael Couck
 * @since 28.12.10
 * @version 01.00
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Field {

	/**
	 * @return whether the field is a field in the Lucene index
	 */
	public boolean field() default true;

}
