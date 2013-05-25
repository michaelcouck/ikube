package ikube.action;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import ikube.AbstractTest;
import ikube.IConstants;

import java.util.ArrayList;
import java.util.List;

import mockit.Deencapsulation;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.05.2013
 * @version 01.00
 */
public class PruneTest extends AbstractTest {

	private Prune prune;

	@Before
	public void before() {
		prune = new Prune();
		Deencapsulation.setField(prune, dataBase);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void internalExecute() {
		List<Object> entities = new ArrayList<Object>();
		when(dataBase.find(any(Class.class), any(String[].class), any(Boolean[].class), anyInt(), anyInt())).thenReturn(entities,
				new ArrayList<Object>());

		addEntities(1, entities);
		prune.internalExecute(indexContext);
		verify(dataBase, atMost(0)).remove(any());

		when(dataBase.find(any(Class.class), any(String[].class), any(Boolean[].class), anyInt(), anyInt())).thenReturn(entities,
				new ArrayList<Object>());
		when(dataBase.count(any(Class.class))).thenReturn(IConstants.MAX_ACTIONS * 10, 0l);
		addEntities(IConstants.MAX_ACTIONS + 10000, entities);
		prune.internalExecute(indexContext);
		verify(dataBase, atLeastOnce()).removeBatch(any(List.class));
	}

	private void addEntities(final long iterations, final List<Object> entities) {
		for (long i = iterations; i > 0; i--) {
			entities.add("an object");
		}
	}

}