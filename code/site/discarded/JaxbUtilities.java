package ikube.toolkit;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * This class has some logic for Jaxb things. At the time of writing the primary concern was to generate an Xsd schema from classes rather
 * than do them by hand constantly.
 * 
 * @author U365981
 * @since 09.05.2012 11:40:32
 * 
 * @revision 01.00
 * @lastChangedBy Michael Couck
 * @lastChangedDate 09.05.2012 11:40:32
 */
public class JaxbUtilities {

	private static final Logger LOGGER = Logger.getLogger(JaxbUtilities.class);

	/**
	 * This method will generate the Xsd schema from the class parameter based on the annotations in the Jaxb objects.
	 * 
	 * @param klass the class to generate the Xsd schema for
	 * @return the xsd schema for the Jaxb object
	 * @throws Exception
	 */
	public static String generateJaxbSchema(final Class<?> klass) throws Exception {
		final File baseDir = new File(".");
		class FileSystemWriterSchemaOutputResolver extends SchemaOutputResolver {
			private File outputFile;

			public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
				outputFile = new File(baseDir, suggestedFileName);
				LOGGER.info("Writing schema to : " + outputFile.getAbsolutePath());
				return new StreamResult(outputFile);
			}

			String getResult() throws IOException {
				return FileUtils.readFileToString(outputFile);
			}
		}
		JAXBContext context = JAXBContext.newInstance(klass);
		FileSystemWriterSchemaOutputResolver resolver = new FileSystemWriterSchemaOutputResolver();
		context.generateSchema(resolver);
		return resolver.getResult();
	}

}