package ru.backtesting.test;

import java.util.Arrays;
import java.util.List;

import ru.backtesting.mktindicators.HilbertTrendlineSignal;
import ru.backtesting.mktindicators.OnBalanceVolumeIndicator;
import ru.backtesting.mktindicators.RSIOscillatorSignal;
import ru.backtesting.mktindicators.base.MarketIndicatorInterface;
import ru.backtesting.mktindicators.base.MarketIndicatorInterval;
import ru.backtesting.mktindicators.base.MarketIndicatorType;
import ru.backtesting.mktindicators.ma.MovingAverageIndicatorSignal;
import ru.backtesting.rebalancing.Frequency;
import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.stockquotes.graphics.GraphicsUtils;
import ru.backtesting.stockquotes.graphics.MarketIndicatorDataSeries;
import ru.backtesting.stockquotes.graphics.MarketQuoteDataSeries;

public class GraphicsUtilsTest {
	public static void main(String[] args) {
		//String pageHtml = testSPY_and_SMA_WMA_And_Trend_Chart();
		
		// String pageHtml = testRSIandSPY_OverlayChart();

		// String pageHtml = testBigChart();

		// String pageHtml = testVXXandSPY_OverlayChart();
		
		String pageHtml =  testSPY_andOBVInd_OverlayChart();
		
		
		System.out.println("chart html = " + pageHtml);

		jXBrowserTest.showHtmlInBrowser(pageHtml);
	}

	private static String testBigChart() {
		String ticker1 = "SPY";
		
		String ticker2 = "TLT";
		
		String htmlSPYwithTLT = simpleTestWithTwoQuotes(ticker1, ticker2, 2018, 2019, Frequency.Monthly);

		String htmlSPY = simpleTestWithSmallChart(ticker1, 2018, 2019, Frequency.Daily);
		
		String htmlRSI14 = simpleTestWithRSI14Chart(ticker1, 2018, 2019, Frequency.Daily, 14, MarketIndicatorInterval.Daily);

		String htmlRSI14Double = testWithStockAndRSI14Chart(ticker1, 2018, 2019, Frequency.Daily, 14, MarketIndicatorInterval.Daily);
		
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

	
	private static String testSPY_and_SMA_WMA_And_Trend_Chart() {
		String ticker = "SPY";
		Frequency frequency = Frequency.Weekly;
		MarketIndicatorInterval interval = MarketIndicatorInterval.Weekly;
		
		int startYear = 2015, endYear = 2019;
		
		MarketIndicatorInterface sma50 = new MovingAverageIndicatorSignal(50, MarketIndicatorType.SMA_IND, interval, 1);
		MarketIndicatorInterface sma200 = new MovingAverageIndicatorSignal(200, MarketIndicatorType.SMA_IND, interval, 1);

		MarketIndicatorDataSeries sma50DataSeries = new MarketIndicatorDataSeries(ticker, sma50, 
				sma50.getMarketIndType() + "(" + sma50.getTimePeriod() + ") : " + ticker, startYear, endYear, frequency);
		
		MarketIndicatorDataSeries sma200DataSeries = new MarketIndicatorDataSeries(ticker, sma200, 
				sma200.getMarketIndType() + "(" + sma200.getTimePeriod() + ") : " + ticker, startYear, endYear, frequency);
		
		MarketQuoteDataSeries quotesDataSeries = new MarketQuoteDataSeries(ticker, startYear, endYear, frequency, false);

		MarketIndicatorInterface wma50 = new MovingAverageIndicatorSignal(50, MarketIndicatorType.WMA_IND, interval, 1);
		MarketIndicatorDataSeries wma50DataSeries = new MarketIndicatorDataSeries(ticker, wma50, 
				wma50.getMarketIndType() + "(" + wma50.getTimePeriod() + ") : " + ticker, startYear, endYear, frequency);
		
		MarketIndicatorInterface wma200 = new MovingAverageIndicatorSignal(200, MarketIndicatorType.WMA_IND, interval, 1);
		MarketIndicatorDataSeries wma200DataSeries = new MarketIndicatorDataSeries(ticker, wma200, 
				wma200.getMarketIndType() + "(" + wma200.getTimePeriod() + ") : " + ticker, startYear, endYear, frequency);
		
		MarketIndicatorInterface hilbertTrend = new HilbertTrendlineSignal(1, interval);
		MarketIndicatorDataSeries hilbertTrendDataSeries = new MarketIndicatorDataSeries(ticker, hilbertTrend, 
				hilbertTrend.getMarketIndType() + "(none) : " + ticker, startYear, endYear, frequency);
		
		return GraphicsUtils.createMultipleTimeSeriesChart(
				Arrays.asList(new MarketQuoteDataSeries[] { quotesDataSeries}), 
				Arrays.asList(new MarketIndicatorDataSeries[] { sma50DataSeries, sma200DataSeries, 
						wma50DataSeries, wma200DataSeries, hilbertTrendDataSeries }), 
				"spy/sma(50-200) chart", "dates", ticker);		
	}
	
	private static String testVXXandSPY_OverlayChart() {
		String ticker1 = "SPY";
		String ticker2 = "vix"; // vxx

		Frequency frequency = Frequency.Daily;
		
		MarketQuoteDataSeries quotesDataSeries1 = new MarketQuoteDataSeries(ticker1, 2014, 2019, frequency, false);
		
		MarketQuoteDataSeries quotesDataSeries2 = new MarketQuoteDataSeries(ticker2, 2014, 2019, frequency, false);

				
		return GraphicsUtils.createOverlayTimeSeriesChart(quotesDataSeries1, quotesDataSeries2, 
				"spy/vxx chart", "dates");
	}
	
	private static String testSPY_andOBVInd_OverlayChart() {
		String ticker = "SPX";
		
		Frequency frequency = Frequency.Weekly;
		MarketIndicatorInterval interval = MarketIndicatorInterval.Weekly;
		
		MarketQuoteDataSeries quotesDataSeries = new MarketQuoteDataSeries(ticker, 2018, 2019, frequency, false);
		
		OnBalanceVolumeIndicator obvInd = new OnBalanceVolumeIndicator(interval);
		MarketIndicatorDataSeries obvIndDataSeries = new MarketIndicatorDataSeries(ticker, obvInd, 
				"obv(" + interval + ") : " + ticker, 2018, 2019, frequency);
				
		return GraphicsUtils.createOverlayTimeSeriesChart(quotesDataSeries, obvIndDataSeries, 
				"spy/obv chart", "dates");
	}
	
	private static String testRSIandSPY_OverlayChart() {
		String ticker = "MTUM";
		int period = 14;
		Frequency frequency = Frequency.Daily;
		MarketIndicatorInterval interval = MarketIndicatorInterval.Daily;
		
		MarketIndicatorInterface rsiOsc = new RSIOscillatorSignal(period, interval);

		MarketQuoteDataSeries quotesDataSeries = new MarketQuoteDataSeries(ticker, 2018, 2019, frequency, false);
		
		MarketIndicatorDataSeries rsiIndDataSeries = new MarketIndicatorDataSeries(ticker, rsiOsc, 
				"rsi(" + period + ") : " + ticker, 2018, 2019, frequency);
				
		return GraphicsUtils.createOverlayTimeSeriesChart(quotesDataSeries, rsiIndDataSeries, 
				"spy/rsi(" + period + ") chart", "dates");
	}
	
	private static String testWithStockAndRSI14Chart(String ticker, int startYear, int endYear, Frequency frequency, int rsiPeriod, MarketIndicatorInterval inverval) {
		MarketIndicatorInterface rsiOsc = new RSIOscillatorSignal(rsiPeriod, inverval);
		
		MarketIndicatorDataSeries rsiIndDataSeries = new MarketIndicatorDataSeries(ticker, rsiOsc, 
				"rsi(" + rsiPeriod + ") : " + ticker, startYear, endYear, frequency);
		
		MarketQuoteDataSeries quotesDataSeries = new MarketQuoteDataSeries(ticker, startYear, endYear, frequency, false);
		
		return GraphicsUtils.createMultipleTimeSeriesChart(Arrays.asList(new MarketQuoteDataSeries[] { quotesDataSeries}), 
				Arrays.asList(new MarketIndicatorDataSeries[] { rsiIndDataSeries}), 
				"simple graphic spy/tlt", "dates: " + startYear + " - " + endYear, "spy/tlt");
	}
	
	private static String simpleTestWithRSI14Chart(String ticker, int startYear, int endYear, Frequency frequency, int period, MarketIndicatorInterval inverval) {
		MarketIndicatorInterface rsiOsc = new RSIOscillatorSignal(period, inverval);
		
		MarketIndicatorDataSeries rsiIndDataSeries = new MarketIndicatorDataSeries(ticker, rsiOsc, 
				"rsi(" + period + ") : " + ticker, startYear, endYear, frequency);
		
		return GraphicsUtils.createIndicatorTimeSeriesChart(rsiIndDataSeries, "rsi(14)", 
				"dates: " + startYear + " - " + endYear);
	}
	
	private static String simpleTestWithSmallChart(String ticker, int startYear, int endYear, Frequency frequency) {
		StockQuoteHistory.storage().fillQuotesData(ticker, startYear, endYear);
		
		MarketQuoteDataSeries quotesDataSeries = new MarketQuoteDataSeries(ticker, startYear, endYear, frequency, false);
		
		
		return GraphicsUtils.createSmallTimeSeriesChart(quotesDataSeries, 
				"simple graphic " + ticker, "dates: " + startYear + " - " + endYear);
	}

	@Deprecated
	private static String simpleTestWithTwoQuotes(String ticker1, String ticker2, int startYear, int endYear, Frequency frequency) {
		MarketQuoteDataSeries quotesDataSeries1 = new MarketQuoteDataSeries(ticker1, startYear, endYear, frequency, false);
		
		MarketQuoteDataSeries quotesDataSeries2 = new MarketQuoteDataSeries(ticker2, startYear, endYear, frequency, false);
		
		List<MarketQuoteDataSeries> quoteDataSeriesList = Arrays.asList(
				new MarketQuoteDataSeries[] { quotesDataSeries1, quotesDataSeries2 });
		
		return GraphicsUtils.createMultipleTimeSeriesChart(quoteDataSeriesList, null, 
				"simple graphic spy/tlt", "dates: " + startYear + " - " + endYear, 
				"spy/tlt");
	}
}
