package ikube.data;

import ikube.AbstractTest;

import org.junit.Test;

public class DatabaseDeleteTest extends AbstractTest {

	@Test
	public void main() {
		DatabaseDelete.main(new String[] { "10000", "IkubePersistenceUnitPostgres", "ikube.model.geospatial.GeoName" });
	}

}