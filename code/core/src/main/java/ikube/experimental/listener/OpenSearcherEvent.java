package ikube.experimental.listener;

import ikube.experimental.Context;
import org.apache.lucene.store.Directory;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 17-08-2015
 */
public class OpenSearcherEvent implements IEvent<Void, Directory[]> {

    private Context context;
    private Directory[] directories;

    public OpenSearcherEvent(final Context context, final Directory[] directories) {
        this.context = context;
        this.directories = directories;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public Void getSource() {
        return null;
    }

    @Override
    public Directory[] getData() {
        return directories;
    }

}