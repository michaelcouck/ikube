package ikube.experimental;

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
		System.out.println(anything);
		anything.something();
	}

}
