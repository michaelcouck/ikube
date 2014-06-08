package ikube.toolkit;

import ikube.Constants;

import java.beans.BeanInfo;
import java.beans.ExceptionListener;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Transient;

import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.Logger;

/**
 * @author Michael Couck
 * @since 21-11-2010
 * @version 01.00
 */
public final class SerializationUtilities {

	private static final Logger LOGGER = Logger.getLogger(SerializationUtilities.class);
	private static ExceptionListener EXCEPTION_LISTENER = new ExceptionListener() {
		@Override
		public void exceptionThrown(final Exception exception) {
			LOGGER.error("General exception : " + exception.getMessage());
			LOGGER.info(null, exception);
		}
	};

	public static String serialize(final Object object) {
		XMLEncoder xmlEncoder = null;
		try {
			SerializationUtilities.setTransientFields(object.getClass(), new ArrayList<Class<?>>());
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			xmlEncoder = new XMLEncoder(byteArrayOutputStream);
			xmlEncoder.setExceptionListener(EXCEPTION_LISTENER);
			xmlEncoder.writeObject(object);
			xmlEncoder.flush();
			xmlEncoder.close();
			xmlEncoder = null;
			return byteArrayOutputStream.toString(Constants.ENCODING);
		} catch (final UnsupportedEncodingException e) {
			LOGGER.error("Unsupported encoding : ", e);
		} catch (final Exception e) {
			LOGGER.error("Exception serializing object : " + object, e);
		} finally {
			if (xmlEncoder != null) {
				xmlEncoder.close();
			}
		}
		return null;
	}

	public static Object deserialize(final String xml) {
		byte[] bytes;
		XMLDecoder xmlDecoder = null;
		try {
			bytes = xml.getBytes(Constants.ENCODING);
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
			xmlDecoder = new XMLDecoder(byteArrayInputStream);
			xmlDecoder.setExceptionListener(EXCEPTION_LISTENER);
			return xmlDecoder.readObject();
		} catch (final UnsupportedEncodingException e) {
			LOGGER.error("Unsupported encoding : ", e);
		} catch (final Exception e) {
			LOGGER.error("Exception de-serialising object : " + xml, e);
		} finally {
			if (xmlDecoder != null) {
				xmlDecoder.close();
			}
		}
		return null;
	}

	public static Object clone(final Object object) {
		return SerializationUtils.clone((Serializable) object);
	}
	
	@SuppressWarnings({"unchecked", "UnusedParameters"})
	public static <T> T clone(final Class<T> klass, T t) {
		return (T) clone(t);
	}

	public static void setTransientFields(Class<?>... classes) {
		List<Class<?>> doneClasses = new ArrayList<>();
		for (Class<?> klass : classes) {
			setTransientFields(klass, doneClasses);
		}
	}

	public static void setTransientFields(final Class<?> klass, final List<Class<?>> doneClasses) {
		Class<?> currentClass = klass;
		do {
			if (doneClasses.contains(currentClass)) {
				return;
			}
			doneClasses.add(currentClass);
			BeanInfo info;
			try {
				info = Introspector.getBeanInfo(currentClass);
			} catch (final IntrospectionException e) {
				LOGGER.error("Exception setting the transient fields in the serializer : ", e);
				return;
			}
			PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
			for (final PropertyDescriptor pd : propertyDescriptors) {
				String fieldName = pd.getName();
				try {
					Field field = SerializationUtilities.getField(currentClass, fieldName);
					if (field == null) {
						continue;
					}
					Transient transientAnnotation = field.getAnnotation(Transient.class);
					boolean isTransient = Modifier.isTransient(field.getModifiers());
					if (transientAnnotation != null || isTransient) {
						field.setAccessible(Boolean.TRUE);
						pd.setValue("transient", Boolean.TRUE);
					}
					if (Collection.class.isAssignableFrom(field.getType())) {
						Type parameterizedType = field.getGenericType();
						if (parameterizedType != null) {
							if (ParameterizedType.class.isAssignableFrom(parameterizedType.getClass())) {
								Type[] typeArguments = ((ParameterizedType) parameterizedType).getActualTypeArguments();
								for (final Type typeArgument : typeArguments) {
									if (ParameterizedType.class.isAssignableFrom(typeArgument.getClass())) {
										Type rawType = ((ParameterizedType) typeArgument).getRawType();
										if (Class.class.isAssignableFrom(rawType.getClass())) {
											setTransientFields((Class<?>) rawType, doneClasses);
										}
									}
								}
							}
						}
					}
				} catch (final SecurityException e) {
					LOGGER.error("Exception setting the transient fields in the serializer : ", e);
				}
			}
			Field[] fields = currentClass.getDeclaredFields();
			for (final Field field : fields) {
				Class<?> fieldClass = field.getType();
				setTransientFields(fieldClass, doneClasses);
			}
			currentClass = currentClass.getSuperclass();
		} while (currentClass != null);
	}

	/**
	 * Gets a field in the class or in the hierarchy of the class.
	 * 
	 * @param klass the original class
	 * @param name the name of the field
	 * @return the field in the object or super classes of the object
	 */
	public static Field getField(final Class<?> klass, final String name) {
		Class<?> currentClass = klass;
		do {
			try {
				return klass.getDeclaredField(name);
			} catch (Exception e) {
				// Swallow
			}
			currentClass = currentClass.getSuperclass();
		} while (currentClass != null);
		return null;
	}

	/**
	 * Singularity.
	 */
	private SerializationUtilities() {
		// Documented
	}

}
