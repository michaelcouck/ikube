package ikube.cluster;

import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.analytics.IAnalyzer;
import ikube.cluster.listener.IListener;
import ikube.model.Attribute;
import ikube.model.IndexContext;
import ikube.scheduling.schedule.Event;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;

import javax.persistence.Column;
import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @see IMonitorService
 * @since 28-12-2010
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public class MonitorService implements IMonitorService {

    private static final Logger LOGGER = Logger.getLogger(MonitorService.class);

    @Autowired
    private IClusterManager clusterManager;

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getIndexNames() {
        @SuppressWarnings("rawtypes")
        Map<String, IndexContext> indexContexts = getIndexContexts();
        List<String> indexNames = new ArrayList<>();
        for (final IndexContext indexContext : indexContexts.values()) {
            indexNames.add(indexContext.getIndexName());
        }
        return indexNames.toArray(new String[indexNames.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getIndexFieldNames(final String indexName) {
        // What we return, a unique set of fields
        IndexContext indexContext = getIndexContext(indexName);
        if (indexContext.getMultiSearcher() != null) {
            Collection<String> fields = IndexManager.getFieldNames(indexContext.getMultiSearcher());
            return fields.toArray(new String[fields.size()]);
        }
        return new String[0];
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("rawtypes")
    public Map<String, IndexContext> getIndexContexts() {
        Map<String, IndexContext> indexContexts = new HashMap<>();
        indexContexts.putAll(ApplicationContextManager.getBeans(IndexContext.class));
        return indexContexts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getFieldNames(final Class<?> indexableClass) {
        final List<String> fieldNames = new ArrayList<>();
        ReflectionUtils.doWithFields(indexableClass, new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(final Field field) throws IllegalArgumentException, IllegalAccessException {
                Column column = field.getAnnotation(Column.class);
                // We only take Java types, no collections or complex types
                if (column == null || !(field.getType().isPrimitive() || field.getType().getPackage().equals(Object.class.getPackage()))) {
                    return;
                }
                fieldNames.add(field.getName());
            }
        });
        return fieldNames.toArray(new String[fieldNames.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getFieldDescriptions(final Class<?> indexableClass) {
        final List<String> fieldDescriptions = new ArrayList<>();
        ReflectionUtils.doWithFields(indexableClass, new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                Attribute attribute = field.getAnnotation(Attribute.class);
                // LOGGER.info("Att : " + attribute + ", " + field + ", " + field.getType() + ", " + field.getType().getPackage());
                if (attribute == null ||
                        !(field.getType().isPrimitive() ||
                                field.getType().getPackage() == null ||
                                field.getType().getPackage().equals(Object.class.getPackage()))) {
                    return;
                }
                fieldDescriptions.add(attribute.description());
            }
        });
        return fieldDescriptions.toArray(new String[fieldDescriptions.size()]);
    }

    /**
     * Accesses the index context by the name.
     *
     * @param indexName the name of the context we are looking for
     * @return the index context with the name or null if not found
     */
    @SuppressWarnings("rawtypes")
    public IndexContext getIndexContext(final String indexName) {
        for (final Map.Entry<String, IndexContext> mapEntry : getIndexContexts().entrySet()) {
            if (mapEntry.getValue() == null || mapEntry.getValue().getIndexName() == null) {
                continue;
            }
            if (mapEntry.getValue().getIndexName().equals(indexName)) {
                return mapEntry.getValue();
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getProperties() {
        String ikubeConfiguration = System.getProperty(IConstants.IKUBE_CONFIGURATION);
        if (ikubeConfiguration == null) {
            ikubeConfiguration = IConstants.IKUBE_DIRECTORY;
            System.setProperty(IConstants.IKUBE_CONFIGURATION, ikubeConfiguration);
        }
        Map<String, String> filesAndProperties = new HashMap<>();
        File dotFolder = new File(ikubeConfiguration);
        List<File> propertyFiles = FileUtilities.findFilesRecursively(dotFolder, new ArrayList<File>(), "spring.properties");
        for (final File propertyFile : propertyFiles) {
            try {
                if (propertyFile == null || !propertyFile.canRead() || propertyFile.isDirectory()) {
                    continue;
                }
                String filePath = propertyFile.getAbsolutePath();
                filePath = FileUtilities.cleanFilePath(filePath);
                String fileContents = FileUtilities.getContents(propertyFile, Integer.MAX_VALUE).toString();
                filesAndProperties.put(filePath, fileContents);
            } catch (Exception e) {
                LOGGER.error("Exception reading property file : " + propertyFile, e);
            }
        }
        return filesAndProperties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperties(final Map<String, String> filesAndProperties) {
        for (final Map.Entry<String, String> mapEntry : filesAndProperties.entrySet()) {
            try {
                File file = FileUtilities.getFile(mapEntry.getKey(), Boolean.FALSE);
                if (file == null || !file.exists() || !file.isFile() || !file.canWrite()) {
                    LOGGER.warn("Can't write to file : " + file);
                    continue;
                }
                FileUtilities.setContents(mapEntry.getKey(), mapEntry.getValue().getBytes());
            } catch (Exception e) {
                LOGGER.error("Exception setting properties in file : " + mapEntry.getKey(), e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void terminateAll() {
        long time = System.currentTimeMillis();
        Event terminateEvent = IListener.EventGenerator.getEvent(Event.TERMINATE_ALL, time, null, Boolean.FALSE);
        LOGGER.info("Sending terminate event for all actions : " + terminateEvent);
        clusterManager.sendMessage(terminateEvent);

        Event takeSnapshotEvent = IListener.EventGenerator.getEvent(Event.TAKE_SNAPSHOT, time, null, Boolean.FALSE);
        clusterManager.sendMessage(takeSnapshotEvent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startupAll() {
        long time = System.currentTimeMillis();
        Event terminateEvent = IListener.EventGenerator.getEvent(Event.STARTUP_ALL, time, null, Boolean.FALSE);
        LOGGER.info("Sending startup event for all actions : " + terminateEvent);
        clusterManager.sendMessage(terminateEvent);

        Event takeSnapshotEvent = IListener.EventGenerator.getEvent(Event.TAKE_SNAPSHOT, time, null, Boolean.FALSE);
        clusterManager.sendMessage(takeSnapshotEvent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(final String indexName) {
        long time = System.currentTimeMillis();
        Event startEvent = IListener.EventGenerator.getEvent(Event.STARTUP, time, indexName, Boolean.FALSE);
        clusterManager.sendMessage(startEvent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void terminate(final String indexName) {
        long time = System.currentTimeMillis();
        Event terminateEvent = IListener.EventGenerator.getEvent(Event.TERMINATE, time, indexName, Boolean.FALSE);
        clusterManager.sendMessage(terminateEvent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cpuThrottling() {
        long time = System.currentTimeMillis();
        Event cpuThrottleEvent = IListener.EventGenerator.getEvent(Event.CPU_LOAD_THROTTLING, time, null, Boolean.FALSE);
        clusterManager.sendMessage(cpuThrottleEvent);

        Event takeSnapshotEvent = IListener.EventGenerator.getEvent(Event.TAKE_SNAPSHOT, time, null, Boolean.FALSE);
        clusterManager.sendMessage(takeSnapshotEvent);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public String[] getAnalyzers() {
        Map<String, IAnalyzer> analyzers = ApplicationContextManager.getBeans(IAnalyzer.class);
        return analyzers.keySet().toArray(new String[analyzers.keySet().size()]);
    }

}