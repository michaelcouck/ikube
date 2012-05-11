package ikube.toolkit;

import ikube.geospatial.GeonamePopulator;

import java.io.File;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessStarter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessStarter.class);

	public static void main(String[] args) throws Exception {
		new ProcessStarter().goToUrl();
	}

	public void goToUrl() throws Exception {
		System.setProperty("http.proxyHost", "proxy.pxpost.netpost");
		System.setProperty("http.proxyPort", "8080");

		URL url = new URL("http://ikube.dyndns.org:8080/ikube");
		String string = FileUtilities.getContents(url.openStream(), Integer.MAX_VALUE).toString();
		System.out.println(string);
	}

	protected static void startProcess() throws Exception {
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
