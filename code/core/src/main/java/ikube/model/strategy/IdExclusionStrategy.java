package ikube.model.strategy;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

import java.lang.reflect.Field;

/**
 * This class is to be added to {@link com.google.gson.Gson} so that it doesn't complain when the
 * sub class over rides a field in the super class, for example in the {@link ikube.model.Search} class
 * that over rides the annotations in the super class {@link ikube.model.Persistable}, the {@link ikube.model.Persistable#id}
 * field.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 07-06-2014
 */
public class IdExclusionStrategy implements ExclusionStrategy {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean shouldSkipField(final FieldAttributes fieldAttributes) {
        String fieldName = fieldAttributes.getName();
        Class<?> theClass = fieldAttributes.getDeclaringClass();
        return isFieldInSuperclass(theClass, fieldName);
    }

    private boolean isFieldInSuperclass(Class<?> subclass, String fieldName) {
        Class<?> superclass = subclass.getSuperclass();
        Field field;
        while (superclass != null) {
            field = getField(superclass, fieldName);
            if (field != null)
                return true;
            superclass = superclass.getSuperclass();
        }
        return false;
    }

    private Field getField(Class<?> theClass, String fieldName) {
        try {
            return theClass.getDeclaredField(fieldName);
        } catch (final Exception e) {
            return null;
        }
    }

    @Override
    public boolean shouldSkipClass(final Class<?> aClass) {
        return false;
    }

}
