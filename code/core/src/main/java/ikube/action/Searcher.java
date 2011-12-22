package ikube.action;

import ikube.model.IndexContext;

/**
 * This class will do a search on the indexes on the index defined in this server. If there are not as many results as expected then a mail
 * will be sent to the administrator.
 * 
 * @author Michael Couck
 * @since 31.10.10
 * @version 01.00
 */
@SuppressWarnings("unused")
public class Searcher extends Action<IndexContext<?>, Boolean> {

	private int start = 0;
	private int end = 10;
	private int iterations = 1;
	private String searchString = "Hello";
	private int resultsSizeMinimum = 0;
	private boolean fragment = Boolean.TRUE;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean execute(final IndexContext<?> indexContext) {
		long actionId = 0;
		try {
			actionId = start(indexContext, "");
			// TODO Re-implement this
		} catch (Exception e) {
			logger.error("Exception searching index : ", e);
		} finally {
			stop(actionId);
		}
		return Boolean.TRUE;
	}

	public void setFragment(boolean fragment) {
		this.fragment = fragment;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public void setResultsSizeMinimum(int resultsSizeMinimum) {
		this.resultsSizeMinimum = resultsSizeMinimum;
	}

	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

}