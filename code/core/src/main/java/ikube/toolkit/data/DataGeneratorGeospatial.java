package ikube.toolkit.data;

import java.io.File;
import java.io.FileReader;
import java.sql.Connection;

import net.sf.flatpack.DataSet;
import net.sf.flatpack.DefaultParserFactory;
import net.sf.flatpack.Parser;

/**
 * @author Michael Couck
 * @since 26.04.2011
 * @version 01.00
 */
public class DataGeneratorGeospatial extends ADataGenerator {

	private File mappingFile;
	private File dataFile;

	public DataGeneratorGeospatial(Connection connection, File mappingFile, File dataFile) {
		super(null);
		this.mappingFile = mappingFile;
		this.dataFile = dataFile;
	}

	/**
	 * {@inheritDoc}
	 */
	public void generate() throws Exception {
		
	}

}
