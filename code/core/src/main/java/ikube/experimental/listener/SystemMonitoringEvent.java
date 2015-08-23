package ikube.experimental.listener;

import ikube.experimental.Context;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 23-08-2015
 */
public class SystemMonitoringEvent implements IEvent<Void, Void> {

    private Context context;

    public SystemMonitoringEvent(final Context context) {
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