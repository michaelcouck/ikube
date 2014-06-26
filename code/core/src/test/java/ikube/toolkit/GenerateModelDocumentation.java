package ikube.toolkit;

import com.google.common.base.Predicate;
import ikube.model.Attribute;
import org.reflections.Reflections;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Set;

/**
 * This class will just look through the entities for the annotation {@link Attribute} then extract the information
 * from each field in the
 * entity and produce an html table of the indexables, the fields, and the description of the fields.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 19-02-2013
 */
public class GenerateModelDocumentation {

    public static void main(String[] args) {
        Set<Class<?>> classes = Reflections.collect("ikube.model", new Predicate<String>() {
            @Override
            public boolean apply(@Nullable final String string) {
                return true;
            }
        }).getTypesAnnotatedWith(Attribute.class);
        String output = new GenerateModelDocumentation().createEntityFieldTable(classes);
        System.out.println(output);
    }

    public String createEntityFieldTable(final Set<Class<?>> classes) {
        for (final Class<?> klass : classes) {
            createEntityTableRow(klass);
        }
        return null;
    }

    private void createEntityTableRow(final Class<?> klass) {
        class ModelAttributeFieldCallback implements ReflectionUtils.FieldCallback {
            @Override
            public void doWith(final Field field) throws IllegalArgumentException, IllegalAccessException {
                Attribute attribute = field.getAnnotation(Attribute.class);
                field.getName();
                attribute.field();
                attribute.description();
            }
        }
        class ModelAttributeFieldFilter implements ReflectionUtils.FieldFilter {
            @Override
            public boolean matches(final Field field) {
                return field.getAnnotation(Attribute.class) != null && !field.getName().equals("id");
            }
        }
        ReflectionUtils.doWithFields(klass, new ModelAttributeFieldCallback(), new ModelAttributeFieldFilter());
    }

}