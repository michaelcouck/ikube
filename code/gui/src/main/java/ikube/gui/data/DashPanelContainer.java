package ikube.gui.data;

import org.springframework.beans.factory.annotation.Configurable;

import com.invient.vaadin.charts.InvientCharts;
import com.invient.vaadin.charts.InvientCharts.DateTimeSeries;
import com.invient.vaadin.charts.InvientChartsConfig;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.Panel;

@Configurable
public class DashPanelContainer extends HierarchicalContainer implements IContainer {

	public void init() {
	}

	@Override
	public void init(final Panel target) {
		InvientCharts chart = new InvientCharts(new InvientChartsConfig());
		chart.getConfig().getTitle().setText("Chart title");
		// chart.getConfig().getGeneralChartConfig().setSpacing( new Spacing( 30, 30, 30, 30 ) );
		chart.getConfig().getGeneralChartConfig().setShadow(true);
		chart.setSizeFull();
		chart.setHeight("500px");
		chart.getConfig().getGeneralChartConfig().setShadow(false);

		InvientChartsConfig.DateTimeAxis xaxis = new InvientChartsConfig.DateTimeAxis();
		xaxis.setShowFirstLabel(false);
		chart.getConfig().addXAxes(xaxis);

		InvientChartsConfig.NumberYAxis yaxis = new InvientChartsConfig.NumberYAxis();
		yaxis.setTitle(new InvientChartsConfig.AxisBase.AxisTitle(""));
		chart.getConfig().addYAxes(yaxis);
		yaxis.setMin(0.0);

		DateTimeSeries series = new DateTimeSeries("Used", InvientCharts.SeriesType.SPLINE, new InvientChartsConfig.SeriesConfig(), true);
		chart.addSeries(series);

		target.addComponent(chart);
	}

}
