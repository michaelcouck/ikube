package ikube.web.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Api {

    String uri() default "";

    String type() default "";

    String description();

    Class<?> consumes() default Object.class;

    Class<?> produces() default Object.class;

}
