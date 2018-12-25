package ru.backtesting.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;

import ru.backtesting.utils.DateUtils;
import ru.backtesting.utils.PortfolioUtils;
import tech.tablesaw.api.DateColumn;
import tech.tablesaw.api.DateTimeColumn;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.api.TimeSeriesPlot;
import tech.tablesaw.plotly.components.Figure;
import tech.tablesaw.plotly.components.Page;
import tech.tablesaw.plotly.display.Browser;
import tech.tablesaw.plotly.traces.BoxTrace;
import tech.tablesaw.plotly.traces.HistogramTrace;

// https://jtablesaw.github.io/tablesaw/userguide/toc

public class TablesawTest {
	private static File tempDir = new File(Paths.get(".").toAbsolutePath() + File.separator + "testoutput");
	
	
	public static void main(String[] args) {
		timeSeriesPlot();
	}
		
	public static void timeSeriesPlot() {
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
				
		Page page = Page.pageBuilder(TimeSeriesPlot.create("Drawdowns", ddTable, "date", "drawdowns", "port name"), 
				"target").build();
        String output = page.asJavascript();
		
        System.out.println(output);
    	
        try {
            try (FileWriter fileWriter = new FileWriter(new File(tempDir + File.separator + "timeSeriesPlot.html"))) {
                fileWriter.write(output);
            }
            //new Browser().browse(new File(tempDir + File.separator + "timeSeriesPlot.html"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        jXBrowserTest.showWebFile(output);
        
		//Plot.show(
		//	    TimeSeriesPlot.create("Drawdowns", ddTable, "date", "drawdowns", "port name"), 
		//	    	new File(tempDir + File.separator + "timeSeriesPlot.html"));
	}
	
	public static void figurePlot() {
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
		Plot.show(new Figure(trace1));		
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
