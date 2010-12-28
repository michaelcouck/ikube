package ikube.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is an 
 * 
 * @author Michael Couck
 * @since 28.12.10
 * @version 01.00
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Field {

	/**
	 * @return
	 */
	public boolean field() default false;

}
