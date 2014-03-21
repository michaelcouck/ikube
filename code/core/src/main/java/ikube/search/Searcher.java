package ikube.search;

import java.io.Serializable;
import java.util.concurrent.Callable;

import static ikube.toolkit.ApplicationContextManager.getBean;

/**
 * This is the distributed searcher that will call the search logic over the wire.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 15-03-2014
 */
public class Searcher implements Callable<ikube.model.Search>, Serializable {

    private ikube.model.Search search;

    public Searcher(final ikube.model.Search search) {
        this.search = search;
    }

    @Override
    public ikube.model.Search call() throws Exception {
        try {
            ISearcherService searcherService = getBean(ISearcherService.class.getName());
            return searcherService.doSearch(search);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

}
