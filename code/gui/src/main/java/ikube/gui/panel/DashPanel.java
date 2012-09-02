package ikube.gui.panel;

import ikube.gui.IConstant;
import ikube.gui.data.IContainer;

import java.util.LinkedHashSet;

import com.invient.vaadin.charts.Color.RGB;
import com.invient.vaadin.charts.InvientCharts;
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
import com.vaadin.ui.Panel;

public class DashPanel extends Panel {

	public DashPanel() {
		setSizeFull();
		getContent().setSizeFull();
		setImmediate(true);

		addChart(this);
	}

	public void setData(Object data) {
		((IContainer) data).setData(this);
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

		InvientCharts chart = new InvientCharts(chartConfig);

		chart.setDescription(IConstant.DASH_CHART);

		// addChart(chart, false, false, false);

		panel.getContent().addComponent(chart);

		// new SelfUpdateSplineThread(chart).start();

		chart.setSizeFull();
		chart.setStyleName("v-chart-min-width");
		chart.setHeight("410px");
	}

}
