package ru.backtesting.stockquotes.graphics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import ru.backtesting.gui.BacktestingAppGUIMain;
import ru.backtesting.stockquotes.graphics.base.FinancialTimeSeriesChartInformation;
import ru.backtesting.utils.DataСonverterForHTMLUse;
import tech.tablesaw.api.DateColumn;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.plotly.components.Axis;
import tech.tablesaw.plotly.components.Axis.AutoRange;
import tech.tablesaw.plotly.components.Axis.Constrain;
import tech.tablesaw.plotly.components.Axis.Spikes;
import tech.tablesaw.plotly.components.Axis.Type;
import tech.tablesaw.plotly.components.Figure;
import tech.tablesaw.plotly.components.Layout;
import tech.tablesaw.plotly.components.Layout.HoverMode;
import tech.tablesaw.plotly.components.Margin;
import tech.tablesaw.plotly.components.Marker;
import tech.tablesaw.plotly.components.Page;
import tech.tablesaw.plotly.traces.ScatterTrace;

public class GraphicsUtils {
	private static final int CHART_TIME_SERIES_MARKER_SIZE = 3;

	
	private static final int CHART_TIME_SERIES_STANDART_WIDTH = 1000;
	private static final int CHART_TIME_SERIES_STANDART_HEIGHT = 800;
	
	private static final int CHART_TIME_SERIES_SMALL_HEIGHT = 200;

	
	private static final String GRAPHICS_HTML_DIV_EL_NAME = "image";
	
	public static String createStandartTimeSeriesChart(MarketQuoteDataSeries quoteDataSeries, 
			String title, String xTitle, String yTitle) {		        
		Layout layout = Layout.builder()
                //.title(title)
                .height(CHART_TIME_SERIES_STANDART_HEIGHT)
                .width(CHART_TIME_SERIES_STANDART_WIDTH)
                .hoverMode(HoverMode.FALSE)
                .showLegend(true)
                .xAxis(Axis.builder()
                        .title(xTitle)
                		.type(Type.DATE)
                        .build())
                .yAxis(Axis.builder()
                		.type(Type.DEFAULT)
                        .title(yTitle)
                        .autoRange(AutoRange.TRUE)
                        .build())
                .build();

		ScatterTrace trace = ScatterTrace.builder(
					DateColumn.create(xTitle, quoteDataSeries.getDatesAsLocalDateArr()), 
					DoubleColumn.create(yTitle, quoteDataSeries.getValuesAsDoubleArr()))
				.text(quoteDataSeries.getTooltipsArr())
				.name(quoteDataSeries.getTicker())
				.marker(Marker.builder()
						.size(CHART_TIME_SERIES_MARKER_SIZE)
						.build())
				.mode(ScatterTrace.Mode.LINE_AND_MARKERS)
				.build();
        
        Figure figure = new Figure(layout, trace);
		
		Page page = Page.pageBuilder(figure, generateUnicDivName()).build();
		
		return page.asJavascript();
	}
	
	public static String createIndicatorTimeSeriesChart(MarketIndicatorDataSeries indicatorDataSeries, 
			String title, String xTitle) {		        
		Margin zeroMargin = Margin.builder()
				.top(0)
				.build();
		
		String indicatorTitle = indicatorDataSeries.getIndicator().getMarketIndType() +
				"(" + indicatorDataSeries.getIndicator().getTimePeriod() + ") - " + indicatorDataSeries.getTicker();
		
		Layout layout = Layout.builder()
                //.title(title)
                .height(CHART_TIME_SERIES_STANDART_HEIGHT)
                .width(CHART_TIME_SERIES_STANDART_WIDTH)
                .margin(zeroMargin)
                .hoverMode(HoverMode.FALSE)
                .showLegend(false)
                .xAxis(Axis.builder()
                        .title(xTitle)
                		.type(Type.DATE)
                        .build())
                .yAxis(Axis.builder()
                		.type(Type.DEFAULT)
                        .title(indicatorTitle)
                        .autoRange(AutoRange.TRUE)
                        .build())
                .build();
		
		ScatterTrace trace = ScatterTrace.builder(
				DateColumn.create(xTitle, indicatorDataSeries.getDatesAsLocalDate()), 
				DoubleColumn.create(indicatorTitle, indicatorDataSeries.getValuesAsDoubleArr()))
				.text(indicatorDataSeries.getTooltipsArr())
				//.name(indicatorTitle)
				.marker(Marker.builder()
						.size(CHART_TIME_SERIES_MARKER_SIZE)
						.build())
				.mode(ScatterTrace.Mode.LINE_AND_MARKERS)
				.build();
        
        Figure figure = new Figure(layout, trace);
		
		Page page = Page.pageBuilder(figure, generateUnicDivName()).build();
		
		return page.asJavascript();
	}
	
	public static String createSmallTimeSeriesChart(MarketQuoteDataSeries quoteDataSeries, 
			String title, String xTitle) {		        
		Margin zeroMargin = Margin.builder()
				.top(0)
				.build();
		
		Layout layout = Layout.builder()
                //.title(title)
                .height(CHART_TIME_SERIES_SMALL_HEIGHT)
                .width(CHART_TIME_SERIES_STANDART_WIDTH)
                .margin(zeroMargin)
                .hoverMode(HoverMode.FALSE)
                .showLegend(false)
                .xAxis(Axis.builder()
                        //.title(xTitle)
                		.type(Type.DATE)
                        .build())
                .yAxis(Axis.builder()
                		.type(Type.DEFAULT)
                        .title(quoteDataSeries.getTicker())
                        .autoRange(AutoRange.TRUE)
                        .build())
                .build();

		ScatterTrace trace = ScatterTrace.builder(
				DateColumn.create(xTitle, quoteDataSeries.getDatesAsLocalDateArr()), 
				DoubleColumn.create(quoteDataSeries.getTicker(), quoteDataSeries.getValuesAsDoubleArr()))
				.text(quoteDataSeries.getTooltipsArr())
				//.name(quoteDataSeries.getTicker())
				.marker(Marker.builder()
						.size(CHART_TIME_SERIES_MARKER_SIZE)
						.build())
				.mode(ScatterTrace.Mode.LINE_AND_MARKERS)
				.build();
        
        Figure figure = new Figure(layout, trace);
		
		Page page = Page.pageBuilder(figure, generateUnicDivName()).build();
		
		return page.asJavascript();
	}
	
	public static String createMultipleTimeSeriesChart(List<MarketQuoteDataSeries> quoteDataSeries, 
			List<MarketIndicatorDataSeries> indicatorDataSeries, String title, String xTitle, String yTitle) {		        
		Spikes xSpikes = Spikes.builder()
				.dash("dot")
				.color("orange")
				.build();
		
		Layout layout = Layout.builder()
                .title(title)
                //.margin(Margin.builder().padding(10).build())
                .height(CHART_TIME_SERIES_STANDART_HEIGHT)
                .width(CHART_TIME_SERIES_STANDART_WIDTH)
                .hoverMode(HoverMode.FALSE)
                .showLegend(true)
                .xAxis(Axis.builder()
                		.type(Type.DATE)
                        .title(xTitle)
                		.spikes(xSpikes)
                        .autoRange(AutoRange.TRUE)
                        //.range(DateUtils.dateFromString("2018-05-17 00:00"), DateUtils.dateFromString("2018-12-17 00:00"))
                        .build())
                .yAxis(Axis.builder()
                		.type(Type.DEFAULT)
                        .title(yTitle)
                        .spikes(xSpikes)
                        .autoRange(AutoRange.TRUE)
                        .constrain(Constrain.DOMAIN)
                        .build())
                .build();

		List<ScatterTrace> traces = new ArrayList<ScatterTrace>();
		
		if (quoteDataSeries != null && quoteDataSeries.size() != 0 )
			for ( MarketQuoteDataSeries quoteData : quoteDataSeries ) {	        	
				traces.add(ScatterTrace.builder(
	                    DateColumn.create("date " + quoteData.getTicker(), quoteData.getDatesAsLocalDateArr()),
	                    DoubleColumn.create("values " + quoteData.getTicker(), quoteData.getValuesAsDoubleArr()))
	                    //.showLegend(true)
	                    .name(quoteData.getTicker())
	                    .text(quoteData.getTooltipsArr())
	    				.marker(Marker.builder()
	    						.size(CHART_TIME_SERIES_MARKER_SIZE)
	    						.build())
	                    .mode(ScatterTrace.Mode.LINE_AND_MARKERS)
	                    .build()
	            );
			}

		if (indicatorDataSeries != null && indicatorDataSeries.size() != 0 )
			for ( MarketIndicatorDataSeries indicatorData : indicatorDataSeries ) {	
				String indicatorTitle = indicatorData.getIndicator().getMarketIndType() +
						"(" + indicatorData.getIndicator().getTimePeriod() + ") - " + indicatorData.getTicker();
				
				traces.add(ScatterTrace.builder(
	                    DateColumn.create("date " + indicatorData.getTicker(), indicatorData.getDatesAsLocalDate()),
	                    DoubleColumn.create("values " + indicatorData.getTicker(), indicatorData.getValuesAsDoubleArr()))
	                    .showLegend(true)
	                    .name(indicatorTitle)
	                    .text(indicatorData.getTooltipsArr())
	    				.marker(Marker.builder()
	    						.size(CHART_TIME_SERIES_MARKER_SIZE / 3)
	    						.build())
	    				.mode(ScatterTrace.Mode.LINE_AND_MARKERS)
	                    .build()
	            );
			}
        
        Figure figure = new Figure(layout, traces.toArray( new ScatterTrace[] {}));
		
		Page page = Page.pageBuilder(figure, generateUnicDivName()).build();
		
		return page.asJavascript();
	}
	
	public static String createOverlayTimeSeriesChart(FinancialTimeSeriesChartInformation quoteDataSeries, 
			FinancialTimeSeriesChartInformation indicatorDataSeries, String title, String xTitle) {	
		String templateFile = new File("WEB-INF" + File.separator + "templates/overlayPlot.html").toURI().toString();

		try {
			String templateHTML = BacktestingAppGUIMain.readFile(templateFile);
			
			templateHTML = templateHTML.replaceAll("##x_title##", "'" + xTitle  + "'");
			
			templateHTML = templateHTML.replaceAll("##trace1_X##", 
					DataСonverterForHTMLUse.printDates(quoteDataSeries.getDatesAsLocalDateArr()));
			
			templateHTML = templateHTML.replaceAll("##trace1_Y##", 
					Arrays.toString(quoteDataSeries.getValuesAsDoubleArr()));
			
			templateHTML = templateHTML.replaceAll("##trace1_text##", 
					DataСonverterForHTMLUse.printTextForHtmlUsed(quoteDataSeries.getValuesAsDoubleArr()));
			
			templateHTML = templateHTML.replaceAll("##trace2_X##", 
					DataСonverterForHTMLUse.printDates(indicatorDataSeries.getDatesAsLocalDateArr()));
			
			templateHTML = templateHTML.replaceAll("##trace2_Y##", 
					Arrays.toString(indicatorDataSeries.getValuesAsDoubleArr()));
			
			templateHTML = templateHTML.replaceAll("##trace2_text##", 
					DataСonverterForHTMLUse.printTextForHtmlUsed(indicatorDataSeries.getValuesAsDoubleArr()));
			
			templateHTML = templateHTML.replaceAll("##div##", generateUnicDivName());

			
			templateHTML = templateHTML.replaceAll("##date_range_begin##", "'" + 
					DataСonverterForHTMLUse.printDate(quoteDataSeries.getDatesAsLocalDate().get(0)) + "'");
			
			templateHTML = templateHTML.replaceAll("##date_range_end##", "'" + 
					DataСonverterForHTMLUse.printDate(quoteDataSeries.getDatesAsLocalDate().get(quoteDataSeries.getDatesAsLocalDate().size() - 1)) + "'");

			// y1 data
			templateHTML = templateHTML.replaceAll("##y1_name##", "'" + quoteDataSeries.getTicker()  + "'");
			templateHTML = templateHTML.replaceAll("##y1_title##", "'" + quoteDataSeries.getTicker()  + "'");

			
			String indicatorTitle = indicatorDataSeries.getTicker();
			
			// y2 data
			if ( indicatorDataSeries instanceof MarketIndicatorDataSeries )
				indicatorTitle = ((MarketIndicatorDataSeries) indicatorDataSeries).getIndicator().getMarketIndType() +
					"(" + ((MarketIndicatorDataSeries)indicatorDataSeries).getIndicator().getTimePeriod() + ") - " + indicatorDataSeries.getTicker();
			
			templateHTML = templateHTML.replaceAll("##y2_name##", "'" + indicatorTitle  + "'");
			templateHTML = templateHTML.replaceAll("##y2_title##", "'" + indicatorTitle  + "'");

			// layout title
			templateHTML = templateHTML.replaceAll("##layot_title##", "'" + 
					title + "'");
			
			return templateHTML;
		} catch (IOException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	private static String generateUnicDivName() {
		return GRAPHICS_HTML_DIV_EL_NAME + UUID.randomUUID().toString().substring(0, 4);
	}
}
