package ikube.gui.data;

import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.gui.IConstant;
import ikube.gui.toolkit.GuiTools;
import ikube.model.IndexContext;
import ikube.model.Search;
import ikube.model.Server;
import ikube.model.Snapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.invient.vaadin.charts.InvientCharts;
import com.invient.vaadin.charts.InvientCharts.DateTimePoint;
import com.invient.vaadin.charts.InvientCharts.DateTimeSeries;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;

@Configurable
public class DashPanelContainer extends AContainer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DashPanelContainer.class);

	@Autowired
	private IDataBase dataBase;
	@Autowired
	private transient IClusterManager clusterManager;

	@Override
	public void setData(final Panel panel, final Object... parameters) {
		Panel leftPanel = (Panel) GuiTools.findComponent(panel, IConstant.DASH_LEFT_PANEL, new ArrayList<Component>());
		for (Server server : clusterManager.getServers().values()) {
			Label label = (Label) GuiTools.findComponent(leftPanel, server.getAddress(), new ArrayList<Component>());
			if (label == null) {
				label = new Label(server.getAddress());
				leftPanel.addComponent(label);
				label.setDescription(server.getAddress());
			}
		}
		// Add some searches to the database
		List<Search> searches = dataBase.find(Search.class, 0, 1000);
		for (Search search : searches) {
			search.setCount((int) (Math.random() * 100d));
			dataBase.merge(search);
		}
		setSearchingData(panel);
		setIndexingData(panel);
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
				lastSnapshotDate = new Date(indexContext.getLastSnapshot().getTimestamp());
			}
			DateTimeSeries seriesData = (DateTimeSeries) chart.getSeries(server.getIp());
			if (seriesData == null) {
				LOGGER.info("Adding series : " + server.getIp());
				seriesData = new DateTimeSeries(server.getIp(), true);
				LinkedHashSet<DateTimePoint> points = new LinkedHashSet<DateTimePoint>();
				seriesData.setSeriesPoints(points);
				chart.addSeries(seriesData);
			}
			DateTimePoint point = new DateTimePoint(seriesData, lastSnapshotDate, searchesPerMinute);
			if (!seriesData.getPoints().contains(point)) {
				seriesData.addPoint(point, seriesData.getPoints().size() > 100);
			}
		}
		// Add a point to the default series to keep it visible
		// DateTimeSeries defaultSeries = (DateTimeSeries) chart.getSeries(IConstant.DEFAULT_TIME_SERIES);
		// defaultSeries.addPoint(new DateTimePoint(defaultSeries, new Date(), 0), Boolean.TRUE);
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
				DateTimeSeries seriesData = (DateTimeSeries) chart.getSeries(seriesKey);
				if (!isWorking(server, indexContext)) {
					if (seriesData != null) {
						LOGGER.info("Removing series : " + seriesKey);
						chart.removeSeries(seriesData);
					}
					continue;
				}
				if (seriesData == null) {
					LOGGER.info("Adding series : " + seriesKey);
					seriesData = new DateTimeSeries(seriesKey, true);
					LinkedHashSet<DateTimePoint> points = new LinkedHashSet<DateTimePoint>();
					seriesData.setSeriesPoints(points);
					chart.addSeries(seriesData);
				}
				for (Snapshot snapshot : indexContext.getSnapshots()) {
					DateTimePoint point = new DateTimePoint(seriesData, new Date(snapshot.getTimestamp()), snapshot.getDocsPerMinute());
					if (!seriesData.getPoints().contains(point)) {
						seriesData.addPoint(point, seriesData.getPoints().size() > 100);
					}
				}
			}
		}
		// Add a point to the default series to keep it visible
		DateTimeSeries defaultSeries = (DateTimeSeries) chart.getSeries(IConstant.DEFAULT_TIME_SERIES);
		defaultSeries.addPoint(new DateTimePoint(defaultSeries, new Date(), 0), Boolean.TRUE);
	}

}