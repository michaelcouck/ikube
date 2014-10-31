package ikube.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2010
 */
public abstract class Schedule implements Runnable {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private long delay = 10;
    private long period = 10;
    private boolean single = Boolean.FALSE;

    public long getDelay() {
        return delay;
    }

    public void setDelay(final long delay) {
        this.delay = delay;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(final long period) {
        this.period = period;
    }

    public boolean isSingle() {
        return single;
    }

    public void setSingle(boolean single) {
        this.single = single;
    }

}
