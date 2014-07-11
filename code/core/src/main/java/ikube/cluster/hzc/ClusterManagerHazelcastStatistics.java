package ikube.cluster.hzc;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.monitor.LocalMapStats;
import ikube.IConstants;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.springframework.util.ReflectionUtils.doWithMethods;

/**
 * This class just prints the statistics for the grid.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 11-07-2014
 */
public class ClusterManagerHazelcastStatistics {

    void printStatistics(final HazelcastInstance hazelcastInstance) {
        printStatistics(hazelcastInstance.getMap(IConstants.IKUBE));
        printStatistics(hazelcastInstance.getMap(IConstants.SEARCH));
        printStatistics(hazelcastInstance.getMap(IConstants.SERVER));
    }

    void printStatistics(final IMap map) {
        System.out.println("Stats for map : " + map.getName() + ", size : " + map.size());
        final LocalMapStats localMapStats = map.getLocalMapStats();
        class MethodCallback implements ReflectionUtils.MethodCallback {
            @Override
            public void doWith(final Method method) throws IllegalArgumentException, IllegalAccessException {
                try {
                    String name = method.getName();
                    Object result = method.invoke(localMapStats);
                    System.out.println("        : " + name.replace("get", "") + " : " + result);
                } catch (final InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        class MethodFilter implements ReflectionUtils.MethodFilter {
            @Override
            public boolean matches(final Method method) {
                return method.getName().startsWith("get") && method.getParameterTypes().length == 0;
            }
        }
        doWithMethods(LocalMapStats.class, new MethodCallback(), new MethodFilter());
    }

}
