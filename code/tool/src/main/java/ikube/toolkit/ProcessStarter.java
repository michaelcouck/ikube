package ikube.toolkit;

import ikube.geospatial.GeonamePopulator;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessStarter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessStarter.class);

	public static void main(String[] args) throws Exception {
		Class<?> klass = GeonamePopulator.class;
		String javaHome = System.getProperty("java.home");
		String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
		String classpath = System.getProperty("java.class.path");
		String className = klass.getCanonicalName();
		ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", classpath, className, "geoname1");
		Process process = builder.start();
		int waitResult = process.waitFor();
		int exitValue = process.exitValue();
		LOGGER.info("Result wait : " + waitResult + ", exit value : " + exitValue);
	}

}
