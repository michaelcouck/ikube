package ikube.experimental.listener;

import ikube.IConstants;
import ikube.cluster.gg.ClusterManagerGridGain;
import ikube.toolkit.THREAD;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 17-08-2015
 */
@Service
@Component
@EnableAsync
@Configuration
public class ListenerManager {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired(required = false)
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Qualifier("ikube.experimental.listener.ListenerManager.listeners")
    private Map<String, Map<String, List<IListener<IEvent<?, ?>>>>> listeners = new HashMap<>();

    @Autowired(required = false)
    @Qualifier("ikube.cluster.gg.ClusterManagerGridGain")
    @SuppressWarnings({"SpringJavaAutowiringInspection", "SpringJavaAutowiredMembersInspection"})
    private ClusterManagerGridGain clusterManager;

    public void add(final IListener<IEvent<?, ?>> listener, String type, String name) {
        synchronized (this) {
            get(type, name).add(listener);
        }
    }

    public void remove(final IListener<IEvent<?, ?>> listener, String type, String name) {
        synchronized (this) {
            get(type, name).remove(listener);
        }
    }

    List<IListener<IEvent<?, ?>>> get(final String type, final String name) {
        Map<String, List<IListener<IEvent<?, ?>>>> listenersForType = listeners.get(type);
        if (listenersForType == null) {
            listenersForType = new HashMap<>();
            listeners.put(type, listenersForType);
        }
        List<IListener<IEvent<?, ?>>> listenersForName = listenersForType.get(name);
        if (listenersForName == null) {
            listenersForName = new ArrayList<>();
            listenersForType.put(name, listenersForName);
        }
        return listenersForName;
    }

    public void fire(final IEvent<?, ?> event, final boolean local) {
        logger.debug("Received event : {}", ToStringBuilder.reflectionToString(event));
        if (local) {
            notify(event);
        } else {
            clusterManager.send(IConstants.IKUBE, event);
        }
    }

    public void notify(final IEvent<?, ?> event) {
        String type = event.getClass().getSimpleName();
        String name = event.getContext().getName();
        List<IListener<IEvent<?, ?>>> listeners = get(type, name);
        logger.debug("Notifying listeners : {}", listeners);
        for (final IListener<IEvent<?, ?>> listener : listeners) {
            final String jobName = Long.toString(System.nanoTime());
            class Notifier implements Runnable {
                public void run() {
                    try {
                        logger.debug("Notifying listener : {}", listener);
                        listener.notify(event);
                    } finally {
                        THREAD.destroy(jobName);
                    }
                }
            }
            THREAD.submit(jobName, new Notifier());
        }
    }

    public void addGridListeners() {
        addTopicListener();
        addQueueListener();
    }

    public void addTopicListener() {
        clusterManager.addTopicListener(IConstants.IKUBE, new IListener<IEvent<?, ?>>() {
            @Override
            public void notify(final IEvent<?, ?> event) {
                fire(event, true);
            }
        });
        logger.info("Added topic listener : ");
    }

    public void addQueueListener() {
        clusterManager.addQueueListener(IConstants.IKUBE, new IListener<IEvent<?, ?>>() {
            @Override
            public void notify(final IEvent<?, ?> event) {
                fire(event, true);
            }
        });
        logger.info("Added queue listener : ");
    }

    public void setListeners(final Map<String, Map<String, List<IListener<IEvent<?, ?>>>>> listeners) {
        this.listeners = listeners;
    }

}