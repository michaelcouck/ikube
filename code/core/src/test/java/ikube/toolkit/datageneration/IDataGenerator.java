package ikube.toolkit.datageneration;

public interface IDataGenerator {
	
	public void before() throws Exception;
	
	public void generate() throws Exception;
	
	public void after() throws Exception;
	
}
