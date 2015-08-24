package ikube.experimental.listener;

import ikube.experimental.Context;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 24-08-2015
 */
public class AnalysisEvent implements IEvent<Void, Void> {

    private Context context;

    public AnalysisEvent(final Context context) {
        this.context = context;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public Void getData() {
        return null;
    }

    @Override
    public Void getSource() {
        return null;
    }

}