package ru.backtesting.test;

import java.time.LocalDate;

import ru.backtesting.utils.DateUtils;
import ru.backtesting.utils.PortfolioUtils;
import tech.tablesaw.api.DateColumn;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.api.TimeSeriesPlot;
import tech.tablesaw.plotly.components.Axis;
import tech.tablesaw.plotly.components.Axis.Spikes;
import tech.tablesaw.plotly.components.Figure;
import tech.tablesaw.plotly.components.Layout;
import tech.tablesaw.plotly.components.Layout.HoverMode;
import tech.tablesaw.plotly.components.Page;
import tech.tablesaw.plotly.traces.BoxTrace;
import tech.tablesaw.plotly.traces.HistogramTrace;
import tech.tablesaw.plotly.traces.ScatterTrace;
import tech.tablesaw.plotly.traces.ScatterTrace.Mode;
import tech.tablesaw.plotly.traces.Trace;

// https://jtablesaw.github.io/tablesaw/userguide/toc

public class TablesawTest {	
	public static void main(String[] args) {
		// timeSeriesPlot();
		
		// histogramTest();
		
		String htmlResult = timeSeriesPlotWithSpikes();
		
        System.out.println("html = " + htmlResult);
        
        jXBrowserTest.showHtmlInBrowser(htmlResult);
	}
	
	public static String timeSeriesPlotWithSpikes() {
		LocalDate[] dates = new LocalDate[] { 
				DateUtils.dateFromString("2009-12-31 00:00"),
				DateUtils.dateFromString("2010-12-31 00:00"),
				DateUtils.dateFromString("2011-12-31 00:00"),
				DateUtils.dateFromString("2012-12-31 00:00"),
				DateUtils.dateFromString("2013-12-31 00:00"),
				DateUtils.dateFromString("2014-12-31 00:00"),
				DateUtils.dateFromString("2015-12-31 00:00"),
				DateUtils.dateFromString("2016-12-31 00:00"),
				DateUtils.dateFromString("2017-12-31 00:00"),
				DateUtils.dateFromString("2018-06-06 00:00"),
				DateUtils.dateFromString("2018-12-31 00:00"),
		};
		
		double[] drawdowns = { -40, -25, -21.5, -18, -12, -7, -9,     -5, -4, -18, -1};
		
		Spikes spikes1 = Spikes.builder()
				.dash("solid")
				.color("yellow")
				.build();
		
		Spikes spikes2 = Spikes.builder()
				.dash("dot")
				.color("blue")
				.build();
		
		// put the spikes in the xAxis so we get vertical spikes
		Axis xAxis = Axis.builder()
		    	.spikes(spikes1)
		    	.build();
		// put the xAxis in the builder
		Layout layout = Layout.builder()
		    	.xAxis(xAxis)
		    	.yAxis(Axis.builder()
		    			.spikes(spikes2)
		    			.build())
		    	.hoverMode(HoverMode.CLOSEST)
		    	.build();
		// define your trace
		Trace trace = ScatterTrace.builder(DateColumn.create("dates", dates), 
				DoubleColumn.create("drawdown", drawdowns))
				.mode(Mode.LINE_AND_MARKERS)
				.build();
		
		// put the builder in your figure
		Figure plot = new Figure(layout, trace);

		return Page.pageBuilder(plot, "divName").build().asJavascript();
	}
	
	public static String histogramTest() {
		double[] y1 = {1, 4, 9, 16, 11, 4, 0, 20, 4, 7, 9, 12, 8, 6, 28, 12};
		double[] y2 = {3, 11, 19, 14, 11, 14, 5, 24, -4, 10, 15, 6, 5, 18};

		HistogramTrace trace1 = 	
		    HistogramTrace.builder(y1).opacity(.75).build();
		HistogramTrace trace2 =
		    HistogramTrace.builder(y2).opacity(.75).build();

		Layout layout  = Layout.builder()
		    .barMode(Layout.BarMode.OVERLAY)
		    .build();
		
		return new Figure(layout, trace1, trace2).asJavascript("divName");
	}
		
	public static String timeSeriesPlot() {
		LocalDate[] dates = new LocalDate[] { 
				DateUtils.dateFromString("2009-12-31 00:00"),
				DateUtils.dateFromString("2010-12-31 00:00"),
				DateUtils.dateFromString("2011-12-31 00:00"),
				DateUtils.dateFromString("2012-12-31 00:00"),
				DateUtils.dateFromString("2013-12-31 00:00"),
				DateUtils.dateFromString("2014-12-31 00:00"),
				DateUtils.dateFromString("2015-12-31 00:00"),
				DateUtils.dateFromString("2016-12-31 00:00"),
				DateUtils.dateFromString("2017-12-31 00:00"),
				DateUtils.dateFromString("2018-06-06 00:00"),
				DateUtils.dateFromString("2018-12-31 00:00"),
				DateUtils.dateFromString("2009-12-31 00:00"),
				DateUtils.dateFromString("2010-12-31 00:00"),
				DateUtils.dateFromString("2011-12-31 00:00"),
				DateUtils.dateFromString("2012-12-31 00:00"),
				DateUtils.dateFromString("2013-12-31 00:00"),
				DateUtils.dateFromString("2014-12-31 00:00"),
				DateUtils.dateFromString("2015-12-31 00:00"),
				DateUtils.dateFromString("2016-12-31 00:00"),
				DateUtils.dateFromString("2017-12-31 00:00"),
				DateUtils.dateFromString("2018-06-06 00:00"),
				DateUtils.dateFromString("2018-12-31 00:00")
		};
		
		double[] drawdowns = {
				-40, -25, -21.5, -18, -12, -7, -9,     -5, -4, -18, -1, 
				-20, -12, -11.5, -9,   -6, -6, -3.5, -2.5, -2, -15, -0.5};
 				
		String[] portNames = new String[] { 
				"port 1", "port 1", "port 1", "port 1", "port 1", "port 1", "port 1", "port 1", "port 1", "port 1", "port 1", 
				"port 2", "port 2", "port 2", "port 2"," port 2", "port 2", "port 2", "port 2", "port 2", "port 2", "port 2"};
		
		
		Table ddTable = Table.create("Drawdowns").addColumns(
				DateColumn.create("date", dates), 
				DoubleColumn.create("drawdowns", drawdowns), 
				StringColumn.create("port name", portNames));
				
		return Page.pageBuilder(TimeSeriesPlot.create("Drawdowns", ddTable, "date", "drawdowns", "port name"), 
				"image").build().asJavascript();
	}
	
	public static String figurePlot() {
		double[] values = {1, 2, 3, 7, 9, 11};
		DoubleColumn column = DoubleColumn.create("my numbers", values);
		System.out.println(column.print());
		
		String[] animals = {"bear", "cat", "giraffe"};
		double[] cuteness = {90, 84, 99};

		Table cuteAnimals = Table.create("Cute Animals")
			.addColumns(
				StringColumn.create("Animal types", animals),
				DoubleColumn.create("rating", cuteness));
		
		System.out.println(cuteAnimals.structure());
		
		gagrTest();
		
		double[] y = {1, 4, 9, 16, 11, 4, -1, 20, 4, 7, 9, 12, 8, 6};

		HistogramTrace trace = HistogramTrace.builder(y).build();
		Plot.show(new Figure(trace));
		
		Object[] x = {"sheep", "cows", "fish", "tree sloths", 
				"sheep", "cows", "fish", "tree sloths", 
				"sheep", "cows", "fish", "tree sloths"};
			double[] z = {1, 4, 9, 16, 3, 6, 8, 8, 2, 4, 7, 11};

		BoxTrace trace1 = BoxTrace.builder(x, z).build();
		return new Figure(trace1).asJavascript("divName");		
	}
	
	public static void gagrTest() {
		LocalDate begDay = LocalDate.parse("2007-09-30");
		LocalDate lastDay = LocalDate.parse("2018-12-31");

		double gagr = PortfolioUtils.CAGRInPercent(10000, 24549, begDay, lastDay);
		
		System.out.println(gagr);
		
		begDay = LocalDate.parse("2013-06-01");
		lastDay = LocalDate.parse("2018-09-09");
		
		gagr = PortfolioUtils.CAGRInPercent(10000, 16897.14, begDay, lastDay);
			
		System.out.println(gagr);
	}
}
