package ikube.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Michael Couck
 * @since 15.09.12
 * @version 01.00
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Attribute {

	/**
	 * @return whether the field is a field in the Lucene index
	 */
	boolean field() default true;

	/**
	 * @return the description of this particular field in the model
	 */
	String description() default "No description defined";

	/**
	 * @return the name of the field this is related to
	 */
	String name() default "";

}