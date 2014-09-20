package ikube.use;

import ikube.toolkit.HashUtilities;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.filters.Filter;
import weka.filters.UnsupervisedFilter;

import java.util.Enumeration;

/**
 * This is the start of a filter for string to double. Not complete at the time...
 *
 * @author Michael Couck
 * @version 01.00
 * @since 18-09-2014
 */
public class StringToDouble extends Filter implements UnsupervisedFilter, OptionHandler {

    @Override
    public Instances getOutputFormat() {
        Instances instances = this.getInputFormat();
        Instances outputInstances = new Instances(instances);
        for (int i = 0; i < instances.numInstances(); i++) {
            Instance instance = instances.instance(i);
            for (int j = 0; j < instance.numValues(); j++) {
                Attribute attribute = instance.attribute(j);
                if (attribute.isString()) {
                    String stringValue = instance.stringValue(attribute);
                    double doubleValue = HashUtilities.hash(stringValue);
                    instance.setValue(j, doubleValue);
                }
            }
            outputInstances.add(instance);
        }
        return outputInstances;
    }

    @Override
    public Enumeration listOptions() {
        return null;
    }

    @Override
    public String[] getOptions() {
        return new String[0];
    }

    @Override
    public void setOptions(final String[] options) throws Exception {

    }
}
