package ikube.toolkit;

import ikube.model.Attribute;
import ikube.model.Persistable;
import org.reflections.Reflections;
import org.springframework.util.ReflectionUtils;

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
        Reflections reflections = new Reflections("ikube.model");
        Set<Class<? extends Persistable>> classes = reflections.getSubTypesOf(Persistable.class);
        new GenerateModelDocumentation().createEntityFieldTable(classes);
    }

    public void createEntityFieldTable(final Set<Class<? extends Persistable>> classes) {
        for (final Class<? extends Persistable> klass : classes) {
            createEntityTableRow(klass);
        }
    }

    private void createEntityTableRow(final Class<? extends Persistable> klass) {
        System.out.println("* Class : " + klass.getName());
        System.out.println("");
        class ModelAttributeFieldCallback implements ReflectionUtils.FieldCallback {
            @Override
            public void doWith(final Field field) throws IllegalArgumentException, IllegalAccessException {
                Attribute attribute = field.getAnnotation(Attribute.class);
                System.out.println("** Field name : " + field.getName());
                System.out.println("*** Lucene field : " + attribute.field());
                System.out.println("*** Field description : " + attribute.description());
            }
        }
        class ModelAttributeFieldFilter implements ReflectionUtils.FieldFilter {
            @Override
            public boolean matches(final Field field) {
                return field.getAnnotation(Attribute.class) != null && !field.getName().equals("id");
            }
        }
        ReflectionUtils.doWithFields(klass, new ModelAttributeFieldCallback(), new ModelAttributeFieldFilter());
        System.out.println("");
        System.out.println("");
    }

}