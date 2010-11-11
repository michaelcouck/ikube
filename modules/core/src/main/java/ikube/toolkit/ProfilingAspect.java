package ikube.toolkit;

import ikube.listener.IListener;
import ikube.listener.ListenerManager;
import ikube.model.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.springframework.core.Ordered;

/**
 * This aspect is a simple profiling aspect. This aspect can be used to profile all the methods and gather the overall times to at least
 * give an indication as to where the time is going. Essentially there can be a timer that can dump the data from time so time.
 *
 * @author Michael Couck
 * @since 28.10.10
 * @version 01.00
 */
public class ProfilingAspect implements IListener, Ordered {

	class TimedMethod {
		String methodName;
		long totalMethodTime;
		long totalMethodInvocations;
		long averageMethodTime;
		long percentageMethodTime;
	}

	private Logger logger;
	private Map<String, TimedMethod> timedMethods;

	public ProfilingAspect() {
		this.logger = Logger.getLogger(this.getClass());
		this.timedMethods = new HashMap<String, TimedMethod>();
		ListenerManager.addListener(this);
	}

	public Object profile(ProceedingJoinPoint call) throws Throwable {
		// Start clock
		long start = System.nanoTime();
		try {
			return call.proceed();
		} catch (Throwable e) {
			logger.error("Exception calling aspect method : ", e);
			throw e;
		} finally {
			// Stop clock
			String mathodName = call.toString();
			long end = System.nanoTime();
			long duration = end - start;
			TimedMethod timedMethod = timedMethods.get(mathodName);
			if (timedMethod == null) {
				timedMethod = new TimedMethod();
				timedMethod.methodName = mathodName;
				timedMethod.totalMethodInvocations++;
				timedMethod.totalMethodTime += duration;
				timedMethods.put(timedMethod.methodName, timedMethod);
			} else {
				timedMethod.totalMethodInvocations++;
				timedMethod.totalMethodTime += duration;
			}
		}
	}

	@Override
	public void handleNotification(Event event) {
		if (event.getType().equals(Event.PROFILING)) {
			prettyPrint();
		}
	}

	public void prettyPrint() {
		// Get the total time for all the methods
		long totalTime = getTotalTime(timedMethods);
		if (totalTime == 0) {
			totalTime = 1;
		}
		List<TimedMethod> timedMethods = new ArrayList<TimedMethod>(this.timedMethods.values());

		calculateAverages(totalTime, timedMethods);

		Document document = DocumentFactory.getInstance().createDocument();
		Element htmlElement = document.addElement("html");
		Element bodyElement = htmlElement.addElement("body");
		Element headingElement = bodyElement.addElement("h3");
		headingElement.setText("Total time : " + TimeUnit.NANOSECONDS.toMillis(totalTime));

		allFields(timedMethods, bodyElement);
		byTotalTime(timedMethods, bodyElement);
		byPercentageTime(timedMethods, bodyElement);
		byAverageTime(timedMethods, bodyElement);
		byMostUsed(timedMethods, bodyElement);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		String xml = document.asXML();
		String filePath = "./profiling/profile-" + dateFormat.format(new Date()) + ".xml";
		logger.info("Writing profiling data to : " + filePath);
		FileUtilities.setContents(filePath, xml.getBytes());
	}

	private void calculateAverages(long totalTime, List<TimedMethod> timedMethods) {
		// Calculate the average time and the percentage time for all methods
		for (TimedMethod timedMethod : timedMethods) {
			long averageMethodTime = getAverageTime(timedMethod);
			long percentageMethodTime = getPercentage(totalTime, timedMethod);
			timedMethod.averageMethodTime = averageMethodTime;
			timedMethod.percentageMethodTime = percentageMethodTime;
		}
	}

	private void byMostUsed(List<TimedMethod> timedMethods, Element bodyElement) {
		// Sort them by the most used
		Collections.sort(timedMethods, new Comparator<TimedMethod>() {
			public int compare(TimedMethod o1, TimedMethod o2) {
				return o1.totalMethodInvocations > o2.totalMethodInvocations ? -1
						: o1.totalMethodInvocations == o2.totalMethodInvocations ? 0 : 1;
			}
		});
		Element tableElement = createTable(new String[] { "Method Name", "Invocations" }, bodyElement);
		for (TimedMethod timedMethod : timedMethods) {
			Element rowElement = tableElement.addElement("tr");
			Element tableDataElement = rowElement.addElement("td");
			tableDataElement.setText(timedMethod.methodName);
			tableDataElement = rowElement.addElement("td");
			tableDataElement.setText(Long.toString(timedMethod.totalMethodInvocations));
		}
		addBreak(bodyElement);
	}

	private Element createTable(String[] columnNames, Element bodyElement) {
		Element tableElement = bodyElement.addElement("table");
		Element tableHeaderElement = tableElement.addElement("tr");
		for (String columnName : columnNames) {
			Element headingElement = tableHeaderElement.addElement("th");
			headingElement.setText(columnName);
		}
		return tableElement;
	}

	private void byAverageTime(List<TimedMethod> timedMethods, Element bodyElement) {
		// Sort them by the average time
		Collections.sort(timedMethods, new Comparator<TimedMethod>() {
			public int compare(TimedMethod o1, TimedMethod o2) {
				return o1.averageMethodTime > o2.averageMethodTime ? -1 : o1.averageMethodTime == o2.averageMethodTime ? 0 : 1;
			}
		});
		Element tableElement = createTable(new String[] { "Method Name", "Average time" }, bodyElement);
		for (TimedMethod timedMethod : timedMethods) {
			String denomination = " millis";
			long averageMethodTime = TimeUnit.NANOSECONDS.toMillis(timedMethod.averageMethodTime);
			if (averageMethodTime == 0) {
				averageMethodTime = timedMethod.averageMethodTime;
				denomination = " nanos";
			}
			Element rowElement = tableElement.addElement("tr");
			Element tableDataElement = rowElement.addElement("td");
			tableDataElement.setText(timedMethod.methodName);
			tableDataElement = rowElement.addElement("td");
			tableDataElement.setText(Long.toString(timedMethod.averageMethodTime) + denomination);
		}
		addBreak(bodyElement);
	}

	private void byPercentageTime(List<TimedMethod> timedMethods, Element bodyElement) {
		// Sort them by the percentage time
		Collections.sort(timedMethods, new Comparator<TimedMethod>() {
			public int compare(TimedMethod o1, TimedMethod o2) {
				return o1.percentageMethodTime > o2.percentageMethodTime ? -1 : o1.percentageMethodTime == o2.percentageMethodTime ? 0 : 1;
			}
		});
		Element tableElement = createTable(new String[] { "Method Name", "Percentage time" }, bodyElement);
		for (TimedMethod timedMethod : timedMethods) {
			Element rowElement = tableElement.addElement("tr");
			Element tableDataElement = rowElement.addElement("td");
			tableDataElement.setText(timedMethod.methodName);
			tableDataElement = rowElement.addElement("td");
			tableDataElement.setText(Long.toString(timedMethod.percentageMethodTime));
		}
		addBreak(bodyElement);
	}

	private void byTotalTime(List<TimedMethod> timedMethods, Element bodyElement) {
		// Sort them by the total time
		Collections.sort(timedMethods, new Comparator<TimedMethod>() {
			public int compare(TimedMethod o1, TimedMethod o2) {
				return o1.totalMethodTime > o2.totalMethodTime ? -1 : o1.totalMethodTime == o2.totalMethodTime ? 0 : 1;
			}
		});
		Element tableElement = createTable(new String[] { "Method Name", "Total time" }, bodyElement);
		for (TimedMethod timedMethod : timedMethods) {
			long totalMethodTime = TimeUnit.NANOSECONDS.toMillis(timedMethod.totalMethodTime);
			Element rowElement = tableElement.addElement("tr");
			Element tableDataElement = rowElement.addElement("td");
			tableDataElement.setText(timedMethod.methodName);
			tableDataElement = rowElement.addElement("td");
			tableDataElement.setText(Long.toString(totalMethodTime));
		}
		addBreak(bodyElement);
	}

	private void allFields(List<TimedMethod> timedMethods, Element bodyElement) {
		Element tableElement = createTable(new String[] { "All fields - Method Name", "Total time", "Average time", "Percentage time",
				"Invocations" }, bodyElement);
		for (TimedMethod timedMethod : timedMethods) {
			long totalMethodTime = TimeUnit.NANOSECONDS.toMillis(timedMethod.totalMethodTime);
			Element rowElement = tableElement.addElement("tr");
			Element tableDataElement = rowElement.addElement("td");
			tableDataElement.setText(timedMethod.methodName);
			tableDataElement = rowElement.addElement("td");
			tableDataElement.setText(Long.toString(totalMethodTime));
			tableDataElement = rowElement.addElement("td");
			tableDataElement.setText(Long.toString(timedMethod.averageMethodTime));
			tableDataElement = rowElement.addElement("td");
			tableDataElement.setText(Long.toString(timedMethod.percentageMethodTime));
			tableDataElement = rowElement.addElement("td");
			tableDataElement.setText(Long.toString(timedMethod.totalMethodInvocations));
		}
		addBreak(bodyElement);
	}

	private void addBreak(Element bodyElement) {
		bodyElement.addElement("br");
	}

	private long getTotalTime(Map<String, TimedMethod> timedMethods) {
		long totalTime = 0;
		for (String name : timedMethods.keySet()) {
			TimedMethod timedMethod = timedMethods.get(name);
			totalTime += timedMethod.totalMethodTime;
		}
		return totalTime;
	}

	private long getAverageTime(TimedMethod timedMethod) {
		return timedMethod.totalMethodTime / timedMethod.totalMethodInvocations;
	}

	private long getPercentage(long totalTime, TimedMethod timedMethod) {
		return ((timedMethod.totalMethodTime * 100) / totalTime);
	}

	public int getOrder() {
		return 1;
	}

}