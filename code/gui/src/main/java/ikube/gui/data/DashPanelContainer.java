package ikube.gui.data;

import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.gui.Application;
import ikube.gui.IConstant;
import ikube.gui.toolkit.GuiTools;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.model.Snapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.invient.vaadin.charts.InvientCharts;
import com.invient.vaadin.charts.InvientCharts.DateTimePoint;
import com.invient.vaadin.charts.InvientCharts.DateTimeSeries;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;

@Configurable
public class DashPanelContainer extends AContainer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DashPanelContainer.class);

	@Autowired
	private IDataBase dataBase;
	@Autowired
	private transient IClusterManager clusterManager;

	@Override
	public void setData(final Panel panel, final Object... parameters) {
		setServersData(panel);
		setSearchingData(panel);
		setIndexingData(panel);
	}

	private void setServersData(final Panel panel) {
		Accordion accordion = (Accordion) GuiTools.findComponent(panel, IConstant.SERVERS_ACCORDION, new ArrayList<Component>());
		for (Server server : clusterManager.getServers().values()) {
			TextArea textArea = (TextArea) GuiTools.findComponent(panel, server.getAddress(), new ArrayList<Component>());
			if (textArea == null) {
				textArea = new TextArea();
				textArea.setHeight(160, Sizeable.UNITS_PIXELS);
				textArea.setWidth(100, Sizeable.UNITS_PERCENTAGE);
				textArea.setDescription(server.getAddress());
				Resource serverIcon = new ClassResource(this.getClass(), "/images/icons/server.gif", Application.getApplication());
				accordion.addTab(textArea, server.getAddress(), serverIcon);
			}
			setServerData(server, textArea);
		}
	}

	private void setServerData(final Server server, final TextArea textArea) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Age : ");
		stringBuilder.append(new SimpleDateFormat("dd/MM/yyyy").format(new Date(server.getAge())));
		stringBuilder.append("\n");
		stringBuilder.append("Architecture : ");
		stringBuilder.append(server.getArchitecture());
		stringBuilder.append("\n");
		stringBuilder.append("Free disk space : ");
		stringBuilder.append(server.getFreeDiskSpace());
		stringBuilder.append("\n");
		stringBuilder.append("Free memory : ");
		stringBuilder.append(server.getFreeMemory() / 1000000);
		stringBuilder.append("\n");
		stringBuilder.append("Max memory : ");
		stringBuilder.append(server.getMaxMemory() / 1000000);
		stringBuilder.append("\n");
		stringBuilder.append("Total memory : ");
		stringBuilder.append(server.getTotalMemory() / 1000000);
		stringBuilder.append("\n");
		stringBuilder.append("Processors : ");
		stringBuilder.append(server.getProcessors());
		stringBuilder.append("\n");
		stringBuilder.append("Cpu load : ");
		stringBuilder.append(server.getAverageCpuLoad());
		long totalDocuments = 0;
		for (IndexContext<?> indexContext : server.getIndexContexts()) {
			totalDocuments += indexContext.getLastSnapshot().getNumDocs();
		}
		stringBuilder.append("\n");
		stringBuilder.append("Total documents : ");
		stringBuilder.append(totalDocuments);
		textArea.setValue(stringBuilder.toString());
	}

	private void setSearchingData(final Panel panel) {
		InvientCharts chart = (InvientCharts) GuiTools.findComponent(panel, IConstant.DASH_SEARCHING_CHART, new ArrayList<Component>());
		Map<String, Server> servers = clusterManager.getServers();
		for (Map.Entry<String, Server> mapEntry : servers.entrySet()) {
			Server server = mapEntry.getValue();
			long searchesPerMinute = 0;
			Date lastSnapshotDate = new Date();
			for (IndexContext<?> indexContext : mapEntry.getValue().getIndexContexts()) {
				for (Snapshot snapshot : indexContext.getSnapshots()) {
					searchesPerMinute += snapshot.getSearchesPerMinute();
				}
				if (indexContext.getLastSnapshot() != null) {
					lastSnapshotDate = new Date(indexContext.getLastSnapshot().getTimestamp());
				}
			}
			addPoint(chart, server.getIp(), lastSnapshotDate, searchesPerMinute);
		}
	}

	private void setIndexingData(final Panel panel) {
		InvientCharts chart = (InvientCharts) GuiTools.findComponent(panel, IConstant.DASH_INDEXING_CHART, new ArrayList<Component>());
		Map<String, Server> servers = clusterManager.getServers();
		for (Map.Entry<String, Server> mapEntry : servers.entrySet()) {
			Server server = mapEntry.getValue();
			String serverIp = server.getIp();
			for (IndexContext<?> indexContext : mapEntry.getValue().getIndexContexts()) {
				String indexName = indexContext.getIndexName();
				String seriesKey = serverIp + "-" + indexName;
				if (!isWorking(server, indexContext)) {
					if (chart.getSeries(seriesKey) != null) {
						LOGGER.debug("Removing series : {} ", seriesKey);
					}
					chart.removeSeries(seriesKey);
					continue;
				}
				for (Snapshot snapshot : indexContext.getSnapshots()) {
					addPoint(chart, seriesKey, new Date(snapshot.getTimestamp()), snapshot.getDocsPerMinute());
				}
			}
		}
	}

	private void addPoint(final InvientCharts chart, final String seriesKey, final Date date, final double pointData) {
		DateTimeSeries seriesData = (DateTimeSeries) chart.getSeries(seriesKey);
		if (seriesData == null) {
			LOGGER.info("Adding series : " + seriesKey);
			seriesData = new DateTimeSeries(seriesKey, true);
			LinkedHashSet<DateTimePoint> points = new LinkedHashSet<DateTimePoint>();
			seriesData.setSeriesPoints(points);
			chart.addSeries(seriesData);
		}
		DateTimePoint point = new DateTimePoint(seriesData, date, pointData);
		if (!seriesData.getPoints().contains(point)) {
			seriesData.addPoint(point);
		}
		long maxPoints = 90;
		int pointSize = seriesData.getPoints().size();
		boolean purge = pointSize >= maxPoints;
		if (purge) {
			// Remove some points from the series so it can be seen on the graph
			Collection<DateTimePoint> dateTimePoints = seriesData.getPoints();
			Iterator<InvientCharts.DateTimePoint> iterator = seriesData.getPoints().iterator();
			while (iterator.hasNext() && dateTimePoints.size() > (maxPoints * 0.75)) {
				iterator.remove();
			}
		}
	}

}