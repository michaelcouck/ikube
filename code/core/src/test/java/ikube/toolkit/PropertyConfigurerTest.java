package ikube.toolkit;

import static org.junit.Assert.assertNotNull;
import ikube.AbstractTest;

import java.io.File;
import java.util.jar.JarFile;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 27-03-2011
 * @version 01.00
 */
public class PropertyConfigurerTest extends AbstractTest {

	private String fileBatchSize = "file.batch.size";
	private PropertyConfigurer propertyConfigurer;

	@Before
	public void before() {
		propertyConfigurer = new PropertyConfigurer();
		propertyConfigurer.setFileNamePattern("spring.properties");
	}

	@Test
	public void checkJarFile() {
		File[] jarFiles = FILE.findFiles(new File("."), "ikube-core-");
		if (jarFiles == null || jarFiles.length == 0) {
			return;
		}
		for (File jarFile : jarFiles) {
			logger.error("Jar file : " + jarFile);
			propertyConfigurer.checkJar(jarFile);
		}
		Object property = propertyConfigurer.getProperty(fileBatchSize);
		assertNotNull(property);
	}

	@Test
	public void checkJarJarFile() throws Exception {
		File[] jarFiles = FILE.findFiles(new File("."), "ikube-core-");
		if (jarFiles == null || jarFiles.length == 0) {
			return;
		}
		for (File jarFile : jarFiles) {
			logger.error("Jar file : " + jarFile);
			propertyConfigurer.checkJar(new JarFile(jarFile));
		}
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
