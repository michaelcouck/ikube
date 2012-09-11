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
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class DashPanel extends Panel {

	public DashPanel() {
		setSizeFull();
		getContent().setSizeFull();
		setImmediate(true);

		HorizontalLayout horizontalLayout = new HorizontalLayout();
//		horizontalLayout.setMargin(true);
		horizontalLayout.setSpacing(true);
		horizontalLayout.setSizeFull();
		horizontalLayout.setDescription(IConstant.DASH_HORIZONTAL_PANEL_LAYOUT);

		Panel panel = new Panel();
		panel.setDescription(IConstant.DASH_LEFT_PANEL);
		Label label = new Label("<h3>Servers</h3>", Label.CONTENT_XHTML);
		panel.addComponent(label);

		Accordion accordion = new Accordion();
		accordion.setSizeFull();
		accordion.setDescription(IConstant.SERVERS_ACCORDION);
		panel.addComponent(accordion);

		horizontalLayout.addComponent(panel);
		horizontalLayout.setExpandRatio(panel, .2f);

		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setDescription(IConstant.DASH_PANEL_LAYOUT);
//		verticalLayout.setMargin(true);
		verticalLayout.setSpacing(true);

		addChart(verticalLayout, IConstant.DASH_SEARCHING_CHART, "Searches per second");
		addChart(verticalLayout, IConstant.DASH_INDEXING_CHART, "Documents per second");

		horizontalLayout.addComponent(verticalLayout);
		horizontalLayout.setExpandRatio(verticalLayout, .8f);

		getContent().addComponent(horizontalLayout);
	}

	public void setData(Object data) {
		((IContainer) data).setData(this);
	}

	private void addChart(final VerticalLayout verticalLayout, final String description, final String yAxisTitle) {
		InvientChartsConfig chartConfig = new InvientChartsConfig();
		chartConfig.getGeneralChartConfig().setType(SeriesType.SPLINE);
		chartConfig.getTitle().setText(description);

		DateTimeAxis xAxis = new DateTimeAxis();
		xAxis.setTick(new Tick());
		xAxis.getTick().setPixelInterval(100);
		LinkedHashSet<XAxis> xAxes = new LinkedHashSet<InvientChartsConfig.XAxis>();
		xAxes.add(xAxis);
		chartConfig.setXAxes(xAxes);

		NumberYAxis yAxis = new NumberYAxis();
		yAxis.setTitle(new AxisTitle(yAxisTitle));
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
		chart.setDescription(description);
		chart.setSizeFull();
		chart.setStyleName("v-chart-min-width");
		chart.setHeight(200, Sizeable.UNITS_PIXELS);

		verticalLayout.addComponent(chart);
	}

}