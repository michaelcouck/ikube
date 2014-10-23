package ikube.application;

import com.sun.management.GcInfo;
import ikube.toolkit.ThreadUtilities;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.util.ReflectionUtils;

import javax.management.openmbean.CompositeData;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class GCMXBeanAnalyzer {

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
            for (final GarbageCollectorMXBean garbageCollectorMXBean : garbageCollectorMXBeans) {
                ReflectionUtils.doWithMethods(garbageCollectorMXBean.getClass(), new ReflectionUtils.MethodCallback() {
                    @Override
                    public void doWith(final Method method) throws IllegalArgumentException, IllegalAccessException {
                        try {
                            method.setAccessible(Boolean.TRUE);
                            GcInfo gcInfo = (GcInfo) method.invoke(garbageCollectorMXBean);
                            if (gcInfo == null) {
                                return;
                            }
                            Map<String, MemoryUsage> usageMap = gcInfo.getMemoryUsageBeforeGc();
                            MemoryUsage codeCacheUsage = usageMap.get("Code Cache");
                            codeCacheUsage.getMax();

                            System.out.println(ToStringBuilder.reflectionToString(gcInfo));
                        } catch (final InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }, new ReflectionUtils.MethodFilter() {
                    @Override
                    public boolean matches(final Method method) {
                        return method.getName().equals("getLastGcInfo");
                    }
                });

                System.out.println("---------" + garbageCollectorMXBean.getClass().getName());
                System.out.println("Name of memory manager:" + garbageCollectorMXBean.getName());
                System.out.println("CollectionTime:" + garbageCollectorMXBean.getCollectionTime());
                String[] memoryPoolNames = garbageCollectorMXBean.getMemoryPoolNames();
                for (final String memoryPoolName : memoryPoolNames) {
                    System.out.println("Memory pool name : " + memoryPoolName);
                }
                ThreadUtilities.sleep(1000);
                System.gc();
            }
        }
    }

}