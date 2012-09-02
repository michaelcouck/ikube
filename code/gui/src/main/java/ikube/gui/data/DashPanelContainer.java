package ikube.gui.data;

import ikube.cluster.IClusterManager;
import ikube.gui.IConstant;
import ikube.gui.toolkit.GuiTools;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.model.Snapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.invient.vaadin.charts.InvientCharts;
import com.invient.vaadin.charts.InvientCharts.DateTimePoint;
import com.invient.vaadin.charts.InvientCharts.DateTimeSeries;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;

@Configurable
public class DashPanelContainer extends AContainer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DashPanelContainer.class);

	@Autowired
	private transient IClusterManager clusterManager;

	public void init() {
	}

	@Override
	public void setData(final Panel panel, final Object... parameters) {
		InvientCharts chart = (InvientCharts) GuiTools.findComponent(panel, IConstant.DASH_CHART, new ArrayList<Component>());
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
	}

}