package ikube.gui.data;

import ikube.cluster.IClusterManager;
import ikube.gui.Application;
import ikube.toolkit.ThreadUtilities;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.invient.vaadin.charts.Color.RGB;
import com.invient.vaadin.charts.InvientCharts;
import com.invient.vaadin.charts.InvientCharts.DateTimePoint;
import com.invient.vaadin.charts.InvientCharts.DateTimeSeries;
import com.invient.vaadin.charts.InvientCharts.SeriesType;
import com.invient.vaadin.charts.InvientChartsConfig;
import com.invient.vaadin.charts.InvientChartsConfig.AxisBase.AxisTitle;
import com.invient.vaadin.charts.InvientChartsConfig.AxisBase.NumberPlotLine;
import com.invient.vaadin.charts.InvientChartsConfig.AxisBase.NumberPlotLine.NumberValue;
import com.invient.vaadin.charts.InvientChartsConfig.AxisBase.Tick;
import com.invient.vaadin.charts.InvientChartsConfig.DateTimeAxis;
import com.invient.vaadin.charts.InvientChartsConfig.NumberYAxis;
import com.invient.vaadin.charts.InvientChartsConfig.XAxis;
import com.invient.vaadin.charts.InvientChartsConfig.YAxis;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.Panel;

@Configurable
public class DashPanelContainer extends HierarchicalContainer implements IContainer {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(DashPanelContainer.class);

	private transient InvientCharts chart;
	@Autowired
	private transient IClusterManager clusterManager;

	public void init() {
	}

	@Override
	public void setData(final Panel panel, final Object... parameters) {
		if (chart == null) {
			addChart(panel);
		}
	}

	private void addChart(final Panel panel) {
		InvientChartsConfig chartConfig = new InvientChartsConfig();
		chartConfig.getGeneralChartConfig().setType(SeriesType.SPLINE);

		chartConfig.getTitle().setText("Server indexing performance");

		DateTimeAxis xAxis = new DateTimeAxis();
		xAxis.setTick(new Tick());
		xAxis.getTick().setPixelInterval(150);
		LinkedHashSet<XAxis> xAxes = new LinkedHashSet<InvientChartsConfig.XAxis>();
		xAxes.add(xAxis);
		chartConfig.setXAxes(xAxes);

		NumberYAxis yAxis = new NumberYAxis();
		yAxis.setTitle(new AxisTitle("Documents per second"));
		NumberPlotLine plotLine;
		yAxis.addPlotLine(plotLine = new NumberPlotLine("LineAt0"));
		plotLine.setValue(new NumberValue(0.0));
		plotLine.setWidth(1);
		plotLine.setColor(new RGB(128, 128, 128));
		LinkedHashSet<YAxis> yAxes = new LinkedHashSet<InvientChartsConfig.YAxis>();
		yAxes.add(yAxis);
		chartConfig.setYAxes(yAxes);

		chartConfig.getTooltip().setFormatterJsFunc(
				"function() {" + " return '<b>'+ this.series.name +'</b><br/>'+ "
						+ " $wnd.Highcharts.dateFormat('%Y-%m-%d %H:%M:%S', this.x) +'<br/>'+ "
						+ " $wnd.Highcharts.numberFormat(this.y, 2);" + "}");

		chartConfig.getLegend().setEnabled(false);

		chart = new InvientCharts(chartConfig);

		DateTimeSeries seriesData = new DateTimeSeries("Random Data", true);
		LinkedHashSet<DateTimePoint> points = new LinkedHashSet<InvientCharts.DateTimePoint>();
		Date dtNow = new Date();
		// Add random data.
		for (int cnt = -19; cnt <= 0; cnt++) {
			points.add(new DateTimePoint(seriesData, getUpdatedDate(dtNow, cnt * 10000), Math.random()));
		}
		seriesData.setSeriesPoints(points);
		chart.addSeries(seriesData);

		addChart(chart, false, false, false);

		panel.getContent().addComponent(chart);

		new SelfUpdateSplineThread(chart).start();
	}

	private void addChart(InvientCharts chart, boolean isPrepend, boolean isRegisterEvents, boolean isRegisterSVGEvent) {
		addChart(chart, isPrepend, isRegisterEvents, isRegisterSVGEvent, true);
	}

	private void addChart(InvientCharts chart, boolean isPrepend, boolean isRegisterEvents, boolean isRegisterSVGEvent, boolean isSetHeight) {
		chart.setSizeFull();
		chart.setStyleName("v-chart-min-width");
		if (isSetHeight) {
			chart.setHeight("410px");
		}
	}

	private static Date getUpdatedDate(Date dt, long milliseconds) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(dt.getTime() + milliseconds);
		return cal.getTime();
	}

	private class SelfUpdateSplineThread extends Thread implements Serializable {
		InvientCharts chart;

		SelfUpdateSplineThread(InvientCharts chart) {
			this.chart = chart;
		}

		@Override
		public void run() {
			while (true) {
				ThreadUtilities.sleep(10000);
				synchronized (Application.getApplication()) {
					DateTimeSeries seriesData = (DateTimeSeries) chart.getSeries("Random Data");
					seriesData.addPoint(new DateTimePoint(seriesData, new Date(), Math.random()), true);
				}
			}
		}

	}

}