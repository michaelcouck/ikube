package ikube.analytics;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.util.*;

import static ikube.toolkit.ObjectToolkit.getFieldValue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 27-09-2014
 */
public abstract class Option implements IOption {

    Object[] options;

    protected Option(final Object[] options) {
        this.options = options;

        if (options != null && options.length > 0) {
            Iterator optionsIterator = Arrays.asList(options).iterator();
            List<String> stringOptions = new ArrayList<>();
            while (optionsIterator.hasNext()) {
                Object fieldName = optionsIterator.next();
                if (fieldName.toString().startsWith("-") && optionsIterator.hasNext()) {
                    stringOptions.add(fieldName.toString());
                    stringOptions.add(optionsIterator.next().toString());
                }
            }
            try {
                CmdLineParser parser = new CmdLineParser(this);
                parser.setUsageWidth(4096);
                parser.parseArgument(stringOptions);
            } catch (final CmdLineException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOption(final String name) {
        return (T) getFieldValue(this, name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T getOption(final String name, final Class<T> type) {
        return getOption(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOption(final Class<?>... types) {
        if (types == null || options == null) {
            return null;
        }
        for (final Object option : options) {
            if (isOneOfType(option, types)) {
                // If this is a collection, then check that the
                if (Collection.class.isAssignableFrom(option.getClass())) {
                    if (!isOneOfType(option, types)) {
                        continue;
                    }
                }
                return (T) option;
            }
        }
        return null;
    }

    private boolean isOneOfType(final Object object, final Class<?>... types) {
        if (object == null || types == null) {
            return Boolean.FALSE;
        }
        for (final Class<?> type : types) {
            if (type.isAssignableFrom(object.getClass())) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

}
