package ikube.gui.panel;

import ikube.gui.Application;

import com.invient.vaadin.charts.InvientCharts;
import com.invient.vaadin.charts.InvientCharts.DateTimeSeries;
import com.invient.vaadin.charts.InvientChartsConfig;
import com.vaadin.terminal.ClassResource;
import com.vaadin.ui.Panel;

public class DashPanel extends Panel {

	private static final String ICON_PATH = "/images/Fotolia_11470344_XS_80px.jpg";

	public DashPanel() {
		setSizeFull();
		getContent().setSizeFull();
		setImmediate(true);

		addChart();
	}

	private void addChart() {
		InvientCharts chart = new InvientCharts(new InvientChartsConfig());
		chart.getConfig().getTitle().setText("Chart title");
		// chart.getConfig().getGeneralChartConfig().setSpacing( new Spacing( 30, 30, 30, 30 ) );
		chart.getConfig().getGeneralChartConfig().setShadow(true);
		chart.setSizeFull();
		// chart.setHeight("500px");
		chart.getConfig().getGeneralChartConfig().setShadow(true);

		InvientChartsConfig.DateTimeAxis xaxis = new InvientChartsConfig.DateTimeAxis();
		xaxis.setShowFirstLabel(true);
		xaxis.setTitle(new InvientChartsConfig.AxisBase.AxisTitle("Axis title"));
		chart.getConfig().addXAxes(xaxis);

		InvientChartsConfig.NumberYAxis yaxis = new InvientChartsConfig.NumberYAxis();
		yaxis.setTitle(new InvientChartsConfig.AxisBase.AxisTitle("Base axis"));
		chart.getConfig().addYAxes(yaxis);
		yaxis.setMin(0.0);

		DateTimeSeries series = new DateTimeSeries("Used", InvientCharts.SeriesType.SPLINE, new InvientChartsConfig.SeriesConfig(), true);
		chart.addSeries(series);
		chart.setVisible(true);
		chart.setImmediate(true);
		chart.setIcon(new ClassResource(this.getClass(), ICON_PATH, Application.getApplication()));

		addComponent(chart);
		getContent().addComponent(chart);
	}

}