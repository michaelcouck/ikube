package ikube.web.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Api {

    String uri() default "";

    String description() default "";

    Class<?> consumes() default String.class;

    Class<?> produces() default String.class;

    String type() default "GET";

}
