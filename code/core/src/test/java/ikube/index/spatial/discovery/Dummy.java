package ikube.index.spatial.discovery;

import mockit.Cascading;
import mockit.Mockit;

import org.junit.Before;
import org.junit.Test;

public class Dummy {

	@Cascading
	private Anything anything;

	@Before
	public void before() {
		Mockit.setUpMocks();
	}

	@Test
	public void nothing() {
		anything.something();
		System.out.println(anything);
	}

}
