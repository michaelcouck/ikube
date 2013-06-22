package ikube.toolkit;

import ikube.BaseTest;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

public class AdHocTest extends BaseTest {

	@Test
	@Ignore
	public void print() throws Exception {
		Thread.currentThread().getContextClassLoader();
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("META-INF/maven/ikube/ikube-core/pom.properties");
		logger.info("Input stream : " + inputStream);

		ClassPathResource classPathResource = new ClassPathResource("META-INF/maven/ikube/ikube-core/pom.properties", getClass().getClassLoader());
		inputStream = classPathResource.getInputStream();
		logger.info("Input stream : " + inputStream);

		List<String> lines = IOUtils.readLines(inputStream);
		for (final String line : lines) {
			logger.info("Line : " + line);
		}
		logger.info("Timestamp : " + lines.get(1));
	}

}
