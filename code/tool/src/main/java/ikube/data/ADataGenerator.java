package ikube.data;

import ikube.database.IDataBase;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.Logging;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Blob;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.persistence.Column;
import javax.persistence.Id;

import org.apache.log4j.Logger;
import org.springframework.util.ReflectionUtils;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public abstract class ADataGenerator implements IDataGenerator {

	static {
		Logging.configure();
	}

	private static final long MAX_FILE_LENGTH = 100000;

	protected Logger logger = Logger.getLogger(this.getClass());
	private String wordsFilePath = "english.txt";
	protected List<String> words;
	protected Map<String, byte[]> fileContents;
	protected Map<Class<?>, Object> entities;
	protected IDataBase dataBase;

	public ADataGenerator(IDataBase dataBase) {
		this.dataBase = dataBase;
		entities = new HashMap<Class<?>, Object>();
		words = new ArrayList<String>();
	}

	public void before() throws Exception {
		File dotFolder = new File(".");
		fileContents = new HashMap<String, byte[]>();
		File wordsFile = FileUtilities.findFileRecursively(dotFolder, wordsFilePath);
		populateWords(wordsFile);
		String[] fileTypes = new String[] { ".doc", ".html", ".pdf", ".pot", ".ppt", ".rtf", ".txt", ".xml" };
		List<File> files = FileUtilities.findFilesRecursively(new File("."), new ArrayList<File>(), fileTypes);
		populateFiles(files.toArray(new File[files.size()]), "spring", "svn");
	}

	protected void populateWords(File wordsFile) throws Exception {
		InputStream inputStream = new FileInputStream(wordsFile);
		String words = FileUtilities.getContents(inputStream, Integer.MAX_VALUE).toString();
		StringTokenizer tokenizer = new StringTokenizer(words);
		while (tokenizer.hasMoreTokens()) {
			this.words.add(tokenizer.nextToken());
		}
	}

	protected void populateFiles(File[] files, String... excludedPatterns) {
		outer: for (File file : files) {
			if (file == null || !file.exists() || !file.canRead() || !file.isFile()) {
				continue;
			}
			if (file.length() > MAX_FILE_LENGTH) {
				logger.debug("File too big : " + file.length() + ":" + MAX_FILE_LENGTH);
				continue;
			}
			for (String excludedPattern : excludedPatterns) {
				if (file.getName().contains(excludedPattern)) {
					continue outer;
				}
			}
			// logger.info("Loading file : " + file.getAbsolutePath());
			byte[] contents = FileUtilities.getContents(file, Integer.MAX_VALUE).toByteArray();
			fileContents.put(file.getName(), contents);
			continue;
		}
	}

	protected String generateText(int count, int maxLength) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < count; i++) {
			int index = (int) (Math.random() * (words.size() - 1));
			String word = this.words.get(index);
			builder.append(word);
			builder.append(" ");
		}
		if (builder.length() > maxLength) {
			return builder.substring(0, maxLength);
		}
		return builder.toString();
	}

	@SuppressWarnings("unchecked")
	protected <T> T createInstance(Class<T> klass) throws Exception {
		T entity = (T) entities.remove(klass);
		if (entity == null) {
			entity = klass.newInstance();
			entities.put(klass, entity);
			// Set the fields
			createFields(klass, entity);
		}
		return entity;
	}

	@SuppressWarnings("unchecked")
	protected <C extends Collection<T>, T> C createCollection(Class<C> collectionClass, Class<T> klass) throws Exception {
		Collection<T> collection = collectionClass.newInstance();
		T t = createInstance(klass);
		collection.add(t);
		return (C) collection;
	}

	@SuppressWarnings("unchecked")
	protected <T> T createFields(Class<?> klass, T entity) throws Exception {
		Field[] fields = klass.getDeclaredFields();
		for (Field field : fields) {
			if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
				continue;
			}
			Id id = field.getAnnotation(Id.class);
			if (id != null) {
				continue;
			}
			field.setAccessible(Boolean.TRUE);
			Class<?> fieldClass = field.getType();
			logger.debug("Field class : " + fieldClass);
			Object fieldValue = null;
			// If this is a collection then create the collection
			if (Collection.class.isAssignableFrom(fieldClass)) {
				Type genericType = field.getGenericType();
				Type theActualTypeArgument = null;
				if (ParameterizedType.class.isAssignableFrom(genericType.getClass())) {
					ParameterizedType parameterizedType = (ParameterizedType) genericType;
					Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
					for (Type actualTypeArgument : actualTypeArguments) {
						logger.debug("Actual type argument : " + actualTypeArgument);
						theActualTypeArgument = actualTypeArgument;
					}
				}
				fieldValue = createCollection(ArrayList.class, (Class<?>) theActualTypeArgument);
			} else if (fieldClass.getPackage() == null || fieldClass.getPackage().getName().startsWith("java")) {
				// Java lang class
				Column column = null;
				Annotation[] annotations = field.getAnnotations();
				if (annotations != null) {
					for (Annotation annotation : annotations) {
						if (Column.class.isAssignableFrom(annotation.getClass())) {
							column = (Column) annotation;
							break;
						}
					}
				}
				int length = column != null && column.length() > 0 ? column.length() : 48;
				fieldValue = createInstance(fieldClass, length);
			} else {
				// This is a non Java lang class
				fieldValue = createInstance(fieldClass);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Field : " + field + ", " + fieldClass + ", " + fieldValue);
			}
			ReflectionUtils.setField(field, entity, fieldValue);
		}
		Class<?> superClass = klass.getSuperclass();
		if (!Object.class.equals(superClass)) {
			createFields(superClass, entity);
		}
		return entity;
	}

	protected Object createInstance(Class<?> klass, int length) {
		if (Boolean.class.equals(klass) || boolean.class.equals(klass)) {
			return Boolean.TRUE;
		} else if (Integer.class.equals(klass) || int.class.equals(klass)) {
			return Integer.valueOf((int) System.nanoTime());
		} else if (Long.class.equals(klass) || long.class.equals(klass)) {
			return Long.valueOf(System.nanoTime());
		} else if (Double.class.equals(klass) || double.class.equals(klass)) {
			return new Double(System.nanoTime());
		} else if (Timestamp.class.equals(klass)) {
			return new Timestamp(System.currentTimeMillis());
		} else if (Date.class.equals(klass)) {
			return new Date(System.currentTimeMillis());
		} else if (java.sql.Date.class.equals(klass)) {
			return new java.sql.Date(System.currentTimeMillis());
		} else if (Timestamp.class.equals(klass)) {
			return new Timestamp(System.currentTimeMillis());
		} else if (String.class.equals(klass)) {
			return generateText(length * 5, length);
		} else if (Blob.class.equals(klass)) {
			return new ByteArrayInputStream(generateText(length * 5, length).getBytes());
		} else if (klass.isArray()) {
			if (klass.getName().contains("[B")) {
				String text = generateText(100, length);
				return text.getBytes();
			}
		}
		return null;
	}

	/**
	 * Default empty implementation.
	 */
	public void after() {
		// Sub classes to implement
	}

}
