package ikube.toolkit;

import static org.junit.Assert.*;

import java.io.File;
import java.util.jar.JarFile;

import ikube.ATest;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 27.03.11
 * @version 01.00
 */
public class PropertyConfigurerTest extends ATest {

	private String fileBatchSize = "file.batch.size";
	private PropertyConfigurer propertyConfigurer;

	public PropertyConfigurerTest() {
		super(PropertyConfigurer.class);
	}

	@Before
	public void before() {
		propertyConfigurer = new PropertyConfigurer();
		propertyConfigurer.setFileNamePattern("spring.properties");
	}

	@Test
	public void checkJarFile() {
		File jarFile = FileUtilities.findFileRecursively(new File("."), "ikube-core");
		if (jarFile == null) {
			return;
		}
		propertyConfigurer.checkJar(jarFile);
		Object property = propertyConfigurer.getProperty(fileBatchSize);
		assertNotNull(property);
	}

	@Test
	public void checkJarJarFile() throws Exception {
		File jarFile = FileUtilities.findFileRecursively(new File("."), "ikube-core");
		if (jarFile == null) {
			return;
		}
		propertyConfigurer.checkJar(new JarFile(jarFile));
		Object property = propertyConfigurer.getProperty(fileBatchSize);
		assertNotNull(property);
	}

	@Test
	public void initialize() {
		propertyConfigurer.initialize();
		Object property = propertyConfigurer.get(fileBatchSize);
		assertNotNull(property);
	}

}
