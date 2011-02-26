package ikube.toolkit.datageneration;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public interface IDataGenerator {
	
	public void before() throws Exception;
	
	public void generate() throws Exception;
	
	public void after() throws Exception;
	
}
