package ru.backtesting.test;

import java.util.Arrays;
import java.util.List;

import ru.backtesting.mktindicators.HilbertTrendlineSignal;
import ru.backtesting.mktindicators.OnBalanceVolumeIndicator;
import ru.backtesting.mktindicators.RSIOscillatorSignal;
import ru.backtesting.mktindicators.base.MarketIndicatorInterface;
import ru.backtesting.mktindicators.base.MarketIndicatorType;
import ru.backtesting.mktindicators.ma.MovingAverageIndicatorSignal;
import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.stockquotes.TradingTimeFrame;
import ru.backtesting.stockquotes.graphics.GraphicsUtils;
import ru.backtesting.stockquotes.graphics.MarketIndicatorDataSeries;
import ru.backtesting.stockquotes.graphics.MarketQuoteDataSeries;

public class GraphicsUtilsTest {
	public static void main(String[] args) {
		// String pageHtml = testSPY_and_SMA_WMA_And_Trend_Chart(Frequency.Daily, MarketIndicatorInterval.Daily);
		
		String pageHtml = testRSIandSPY_OverlayChart();

		// String pageHtml = testBigChart();

		//String pageHtml = testVXXandSPY_OverlayChart();
		
		// String pageHtml =  testSPY_andOBVInd_OverlayChart();
		
		// String pageHtml = performanceTest2();
		
		System.out.println("chart html = " + pageHtml);

		
		jXBrowserTest.showHtmlInBrowser(pageHtml);
	}

	private static String performanceTest1() {		
		String pageHtml1 = testSPY_and_SMA_WMA_And_Trend_Chart(TradingTimeFrame.Daily);

		System.out.println("First html = " + pageHtml1);

		String pageHtml2 = testSPY_and_SMA_WMA_And_Trend_Chart(TradingTimeFrame.Weekly);

		System.out.println("Second html = " + pageHtml2);

		return pageHtml1 + "<br><br>" + pageHtml2;
	}
	
	private static String performanceTest2() {
		StockQuoteHistory.storage().loadQuotesData("SPY", TradingTimeFrame.Daily, false);

		String pageHtml1 = createMAGraphics("SPY", 2018, 2019, TradingTimeFrame.Daily);

		System.out.println("First html = " + pageHtml1);

		String pageHtml2 = createMAGraphics("SPY", 2016, 2019, TradingTimeFrame.Weekly);

		System.out.println("Second html = " + pageHtml2);

		return pageHtml1 + "<br><br>" + pageHtml2;
	}
	
	private static String createMAGraphics(String ticker, int startYear, int endYear, TradingTimeFrame period ) {
		MarketIndicatorInterface sma50 = new MovingAverageIndicatorSignal(50, MarketIndicatorType.SMA_IND, period, 1);
		MarketIndicatorInterface sma200 = new MovingAverageIndicatorSignal(200, MarketIndicatorType.SMA_IND, period, 1);

		MarketIndicatorDataSeries sma50DataSeries = new MarketIndicatorDataSeries(ticker, sma50, 
				sma50.getMarketIndType() + "(" + sma50.getTimePeriod() + ") : " + ticker, startYear, endYear, period);
		
		MarketIndicatorDataSeries sma200DataSeries = new MarketIndicatorDataSeries(ticker, sma200, 
				sma200.getMarketIndType() + "(" + sma200.getTimePeriod() + ") : " + ticker, startYear, endYear, period);
		
		MarketQuoteDataSeries quotesDataSeries = new MarketQuoteDataSeries(ticker, startYear, endYear, period, false);

		MarketIndicatorInterface wma50 = new MovingAverageIndicatorSignal(50, MarketIndicatorType.WMA_IND, period, 1);
		MarketIndicatorDataSeries wma50DataSeries = new MarketIndicatorDataSeries(ticker, wma50, 
				wma50.getMarketIndType() + "(" + wma50.getTimePeriod() + ") : " + ticker, startYear, endYear, period);
		
		MarketIndicatorInterface wma200 = new MovingAverageIndicatorSignal(200, MarketIndicatorType.WMA_IND, period, 1);
		MarketIndicatorDataSeries wma200DataSeries = new MarketIndicatorDataSeries(ticker, wma200, 
				wma200.getMarketIndType() + "(" + wma200.getTimePeriod() + ") : " + ticker, startYear, endYear, period);
		
		MarketIndicatorInterface hilbertTrend = new HilbertTrendlineSignal(1, period);
		MarketIndicatorDataSeries hilbertTrendDataSeries = new MarketIndicatorDataSeries(ticker, hilbertTrend, 
				hilbertTrend.getMarketIndType() + "(none) : " + ticker, startYear, endYear, period);
		
		return GraphicsUtils.createMultipleTimeSeriesChart(
				Arrays.asList(new MarketQuoteDataSeries[] { quotesDataSeries}), 
				Arrays.asList(new MarketIndicatorDataSeries[] { sma50DataSeries, sma200DataSeries, 
						wma50DataSeries, wma200DataSeries, hilbertTrendDataSeries }), 
				"spy/moving averages(50-200) chart " + period, "dates", ticker);
	}
	
	private static String testBigChart() {
		String ticker1 = "SPY";
		
		String ticker2 = "TLT";
		
		String htmlSPYwithTLT = simpleTestWithTwoQuotes(ticker1, ticker2, 2018, 2019, TradingTimeFrame.Monthly);

		String htmlSPY = simpleTestWithSmallChart(ticker1, 2018, 2019, TradingTimeFrame.Daily);
		
		String htmlRSI14 = simpleTestWithRSI14Chart(ticker1, 2018, 2019, 14, TradingTimeFrame.Daily);

		String htmlRSI14Double = testWithStockAndRSI14Chart(ticker1, 2018, 2019, 14, TradingTimeFrame.Daily);
		
		String pageHtml = 
				"  <script src=\"https://cdn.plot.ly/plotly-latest.min.js\"></script>" +
				"<table style=\"text-align: left\">\n" + 
				"  		<tr>\n" + 
				"    		<th style=\"font-weight: 800\"><p>График1<p>##graphic1##</th>\n" + 
				"  		</tr>\n" + 
				"		</br>" +
				"  		<tr style=\"margin: 0;padding: 0\">\n" + 
				"    		<th style=\"margin: 0;padding: 0\">##graphic2##</th>\n" + 
				"  		</tr>" +
				"		<tr style=\"margin: 0;padding: 0\">\n" + 
				"    		<th style=\"margin: 0;padding: 0\">##graphic3##</th>\n" + 
				" 		</tr>" +
				"		<tr style=\"margin: 0;padding: 0\">\n" + 
				"    		<th style=\"margin: 0;padding: 0\">##graphic4##</th>\n" + 
				" 		</tr>" +
				"</table>";
		
		pageHtml = pageHtml.replaceAll("##graphic1##", htmlSPYwithTLT);
		pageHtml = pageHtml.replaceAll("##graphic2##", htmlSPY);
		pageHtml = pageHtml.replaceAll("##graphic3##", htmlRSI14);
		pageHtml = pageHtml.replaceAll("##graphic4##", htmlRSI14Double);
		
		return pageHtml;
	}

	
	private static String testSPY_and_SMA_WMA_And_Trend_Chart(TradingTimeFrame period) {
		String ticker = "SPY";
		
		int startYear = 2015, endYear = 2019;
		
		MarketIndicatorInterface sma50 = new MovingAverageIndicatorSignal(50, MarketIndicatorType.SMA_IND, period, 1);
		MarketIndicatorInterface sma200 = new MovingAverageIndicatorSignal(200, MarketIndicatorType.SMA_IND, period, 1);

		MarketIndicatorDataSeries sma50DataSeries = new MarketIndicatorDataSeries(ticker, sma50, 
				sma50.getMarketIndType() + "(" + sma50.getTimePeriod() + ") : " + ticker, startYear, endYear, period);
		
		MarketIndicatorDataSeries sma200DataSeries = new MarketIndicatorDataSeries(ticker, sma200, 
				sma200.getMarketIndType() + "(" + sma200.getTimePeriod() + ") : " + ticker, startYear, endYear, period);
		
		MarketQuoteDataSeries quotesDataSeries = new MarketQuoteDataSeries(ticker, startYear, endYear, period, false);

		MarketIndicatorInterface wma50 = new MovingAverageIndicatorSignal(50, MarketIndicatorType.WMA_IND, period, 1);
		MarketIndicatorDataSeries wma50DataSeries = new MarketIndicatorDataSeries(ticker, wma50, 
				wma50.getMarketIndType() + "(" + wma50.getTimePeriod() + ") : " + ticker, startYear, endYear, period);
		
		MarketIndicatorInterface wma200 = new MovingAverageIndicatorSignal(200, MarketIndicatorType.WMA_IND, period, 1);
		MarketIndicatorDataSeries wma200DataSeries = new MarketIndicatorDataSeries(ticker, wma200, 
				wma200.getMarketIndType() + "(" + wma200.getTimePeriod() + ") : " + ticker, startYear, endYear, period);
		
		MarketIndicatorInterface hilbertTrend = new HilbertTrendlineSignal(1, period);
		MarketIndicatorDataSeries hilbertTrendDataSeries = new MarketIndicatorDataSeries(ticker, hilbertTrend, 
				hilbertTrend.getMarketIndType() + "(none) : " + ticker, startYear, endYear, period);
		
		return GraphicsUtils.createMultipleTimeSeriesChart(
				Arrays.asList(new MarketQuoteDataSeries[] { quotesDataSeries}), 
				Arrays.asList(new MarketIndicatorDataSeries[] { sma50DataSeries, sma200DataSeries, 
						wma50DataSeries, wma200DataSeries, hilbertTrendDataSeries }), 
				"spy/sma(50-200) chart", "dates", ticker);		
	}
	
	private static String testVXXandSPY_OverlayChart() {
		String ticker1 = "SPY";
		String ticker2 = "VXXB"; // vxx

		TradingTimeFrame period = TradingTimeFrame.Daily;
		
		MarketQuoteDataSeries quotesDataSeries1 = new MarketQuoteDataSeries(ticker1, 2014, 2019, period, false);
		
		MarketQuoteDataSeries quotesDataSeries2 = new MarketQuoteDataSeries(ticker2, 2014, 2019, period, false);

				
		return GraphicsUtils.createOverlayTimeSeriesChart(quotesDataSeries1, quotesDataSeries2, 
				"spy/vxx chart", "dates");
	}
	
	private static String testSPY_andOBVInd_OverlayChart() {
		String ticker = "SPX";
		
		TradingTimeFrame period = TradingTimeFrame.Weekly;
		
		MarketQuoteDataSeries quotesDataSeries = new MarketQuoteDataSeries(ticker, 2018, 2019, period, false);
		
		OnBalanceVolumeIndicator obvInd = new OnBalanceVolumeIndicator(period);
		MarketIndicatorDataSeries obvIndDataSeries = new MarketIndicatorDataSeries(ticker, obvInd, 
				"obv(" + period + ") : " + ticker, 2018, 2019, period);
				
		return GraphicsUtils.createOverlayTimeSeriesChart(quotesDataSeries, obvIndDataSeries, 
				"spy/obv chart", "dates");
	}
	
	private static String testRSIandSPY_OverlayChart() {
		String ticker = "TLT";
		int timePeriod = 14;
		TradingTimeFrame period = TradingTimeFrame.Daily;
		
		MarketIndicatorInterface rsiOsc = new RSIOscillatorSignal(timePeriod, period);

		MarketQuoteDataSeries quotesDataSeries = new MarketQuoteDataSeries(ticker, 2014, 2019, period, false);
		
		MarketIndicatorDataSeries rsiIndDataSeries = new MarketIndicatorDataSeries(ticker, rsiOsc, 
				"rsi(" + timePeriod + ") : " + ticker, 2014, 2019, period);
				
		return GraphicsUtils.createOverlayTimeSeriesChart(quotesDataSeries, rsiIndDataSeries, 
				"spy/rsi(" + timePeriod + ") chart", "dates");
	}
	
	private static String testWithStockAndRSI14Chart(String ticker, int startYear, int endYear, int rsiPeriod, TradingTimeFrame period) {
		MarketIndicatorInterface rsiOsc = new RSIOscillatorSignal(rsiPeriod, period);
		
		MarketIndicatorDataSeries rsiIndDataSeries = new MarketIndicatorDataSeries(ticker, rsiOsc, 
				"rsi(" + rsiPeriod + ") : " + ticker, startYear, endYear, period);
		
		MarketQuoteDataSeries quotesDataSeries = new MarketQuoteDataSeries(ticker, startYear, endYear, period, false);
		
		return GraphicsUtils.createMultipleTimeSeriesChart(Arrays.asList(new MarketQuoteDataSeries[] { quotesDataSeries}), 
				Arrays.asList(new MarketIndicatorDataSeries[] { rsiIndDataSeries}), 
				"simple graphic spy/tlt", "dates: " + startYear + " - " + endYear, "spy/tlt");
	}
	
	private static String simpleTestWithRSI14Chart(String ticker, int startYear, int endYear, int timePeriod, TradingTimeFrame period) {
		MarketIndicatorInterface rsiOsc = new RSIOscillatorSignal(timePeriod, period);
		
		MarketIndicatorDataSeries rsiIndDataSeries = new MarketIndicatorDataSeries(ticker, rsiOsc, 
				"rsi(" + timePeriod + ") : " + ticker, startYear, endYear, period);
		
		return GraphicsUtils.createIndicatorTimeSeriesChart(rsiIndDataSeries, "rsi(14)", 
				"dates: " + startYear + " - " + endYear);
	}
	
	private static String simpleTestWithSmallChart(String ticker, int startYear, int endYear, TradingTimeFrame period) {
		StockQuoteHistory.storage().loadQuotesData(ticker, period, false);
		
		MarketQuoteDataSeries quotesDataSeries = new MarketQuoteDataSeries(ticker, startYear, endYear, period, false);
		
		
		return GraphicsUtils.createSmallTimeSeriesChart(quotesDataSeries, 
				"simple graphic " + ticker, "dates: " + startYear + " - " + endYear);
	}

	@Deprecated
	private static String simpleTestWithTwoQuotes(String ticker1, String ticker2, int startYear, int endYear, TradingTimeFrame period) {
		MarketQuoteDataSeries quotesDataSeries1 = new MarketQuoteDataSeries(ticker1, startYear, endYear, period, false);
		
		MarketQuoteDataSeries quotesDataSeries2 = new MarketQuoteDataSeries(ticker2, startYear, endYear, period, false);
		
		List<MarketQuoteDataSeries> quoteDataSeriesList = Arrays.asList(
				new MarketQuoteDataSeries[] { quotesDataSeries1, quotesDataSeries2 });
		
		return GraphicsUtils.createMultipleTimeSeriesChart(quoteDataSeriesList, null, 
				"simple graphic spy/tlt", "dates: " + startYear + " - " + endYear, 
				"spy/tlt");
	}
}
