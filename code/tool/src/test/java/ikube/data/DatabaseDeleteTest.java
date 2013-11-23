package ikube.data;

import ikube.AbstractTest;

import org.junit.Test;

public class DatabaseDeleteTest extends AbstractTest {

	@Test
	public void main() {
		DatabaseDelete.main(new String[] { "10000", "IkubePersistenceUnitH2", "ikube.model.geospatial.GeoName" });
	}

}