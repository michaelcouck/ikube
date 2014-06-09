package ikube.action;


import ikube.AbstractTest;
import ikube.model.File;
import mockit.Deencapsulation;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 02-01-2013
 */
@SuppressWarnings("unchecked")
public class RestTest extends AbstractTest {

	private Reset reset;

	@Before
	public void before() {
		reset = new Reset();
		Deencapsulation.setField(reset, dataBase);
		List<File> files = mock(List.class);
		when(files.size()).thenReturn(1, 0);
		when(dataBase.find(any(Class.class), any(String.class), any(Map.class), anyInt(), anyInt())).thenReturn(files);
	}

	@Test
	public void evaluate() throws Exception {
		reset.execute(indexContext);
		verify(dataBase, atLeastOnce()).removeBatch(any(List.class));
	}

}