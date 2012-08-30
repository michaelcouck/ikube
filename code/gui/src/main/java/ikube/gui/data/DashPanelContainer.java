package ikube.gui.data;

import ikube.cluster.IClusterManager;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.model.Snapshot;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.invient.vaadin.charts.InvientCharts;
import com.invient.vaadin.charts.InvientCharts.DateTimePoint;
import com.invient.vaadin.charts.InvientCharts.DateTimeSeries;
import com.invient.vaadin.charts.InvientChartsConfig;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Panel;

@Configurable
public class DashPanelContainer extends HierarchicalContainer implements IContainer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DashPanelContainer.class);

	@Autowired
	private transient IClusterManager clusterManager;
	private transient InvientCharts chart;

	public void init() {
	}

	@Override
	public void init(final Panel panel) {
		if (chart == null) {
			InvientChartsConfig invientChartsConfig = new InvientChartsConfig();
			invientChartsConfig.setTitle(new InvientChartsConfig.Title());
			chart = new InvientCharts(invientChartsConfig);
			createChart(chart);
			panel.addComponent(chart);
			panel.getContent().addComponent(chart);
		}

		String seriesName = "Performance";
		InvientChartsConfig.SeriesConfig seriesConfig = new InvientChartsConfig.SeriesConfig();

		DateTimeSeries series = (DateTimeSeries) chart.getSeries(seriesName);
		if (series == null) {
			series = new DateTimeSeries(seriesName, InvientCharts.SeriesType.SPLINE, seriesConfig, true);
			chart.addSeries(series);
		}
		series.removeAllPoints();

		Map<String, Server> servers = clusterManager.getServers();
		for (Map.Entry<String, Server> mapEntry : servers.entrySet()) {
			addPoints(mapEntry.getValue(), series);
		}
	}

	private void addPoints(final Server server, final DateTimeSeries series) {
		Random random = new Random();
		LinkedHashSet<DateTimePoint> linkedHashSet = new LinkedHashSet<DateTimePoint>();
		for (IndexContext<?> indexContext : server.getIndexContexts()) {
			int fromIndex = indexContext.getSnapshots().size() > 500 ? indexContext.getSnapshots().size() - 500 : 500;
			int toIndex = indexContext.getSnapshots().size();
			List<Snapshot> snapshots = indexContext.getSnapshots().subList(fromIndex, toIndex);
			for (Snapshot snapshot : snapshots) {
				if (snapshot.getDocsPerMinute() == 0) {
					snapshot.setDocsPerMinute((long) (random.nextDouble() * 10000d));
					LOGGER.info("Snapshot : " + snapshot);
				}
				DateTimePoint dateTimePoint = new DateTimePoint(series, new Date(snapshot.getTimestamp()), snapshot.getDocsPerMinute());
				linkedHashSet.add(dateTimePoint);
				LOGGER.info("Setting date time point : " + dateTimePoint);
			}
		}
		series.setSeriesPoints(linkedHashSet);
	}

	private void createChart(final InvientCharts chart) {
		chart.getConfig().getTitle().setText("Server indexing performance : ");
		chart.setHeight("450px");
		chart.setWidth(100, Sizeable.UNITS_PERCENTAGE);

		chart.getConfig().getGeneralChartConfig().setShadow(true);
		chart.getConfig().getGeneralChartConfig().setShadow(true);
		InvientChartsConfig.DateTimeAxis xaxis = new InvientChartsConfig.DateTimeAxis();
		xaxis.setShowFirstLabel(true);
		xaxis.setTitle(new InvientChartsConfig.AxisBase.AxisTitle("Time in minutes"));
		chart.getConfig().addXAxes(xaxis);
		InvientChartsConfig.NumberYAxis yaxis = new InvientChartsConfig.NumberYAxis();
		yaxis.setTitle(new InvientChartsConfig.AxisBase.AxisTitle("Documents per minute"));
		chart.getConfig().addYAxes(yaxis);
		yaxis.setMin(0.0);

		chart.setVisible(true);
		chart.setImmediate(true);
	}

}