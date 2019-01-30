package ru.backtesting.gui.jshelper;

import java.util.Arrays;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.JSFunction;
import com.teamdev.jxbrowser.chromium.dom.By;
import com.teamdev.jxbrowser.chromium.dom.DOMDocument;
import com.teamdev.jxbrowser.chromium.dom.DOMElement;

import ru.backtesting.mktindicators.HilbertTrendlineSignal;
import ru.backtesting.mktindicators.base.MarketIndicatorInterface;
import ru.backtesting.mktindicators.base.MarketIndicatorInterval;
import ru.backtesting.mktindicators.base.MarketIndicatorType;
import ru.backtesting.mktindicators.ma.MovingAverageIndicatorSignal;
import ru.backtesting.rebalancing.Frequency;
import ru.backtesting.stockquotes.graphics.GraphicsUtils;
import ru.backtesting.stockquotes.graphics.MarketIndicatorDataSeries;
import ru.backtesting.stockquotes.graphics.MarketQuoteDataSeries;
import ru.backtesting.utils.Logger;

public class RecomendationPageJSHelper {
	private Browser browser;
	private boolean isCalcSpyMovingAverageGraphics;
	
	public RecomendationPageJSHelper(Browser browser) {
		this.browser = browser;
		isCalcSpyMovingAverageGraphics = false;
	}
	
	public void loadSpyMovingAverageGraphics() {
		Logger.log().info("Вызов метода \"loadSpyMovingAverageGraphics\"");
				
		if ( isCalcSpyMovingAverageGraphics == false ) {
			DOMDocument document = browser.getDocument();
			
			DOMElement spyMADailyEl = document.findElement(By.id("spyMADailyGraph"));

			DOMElement spyMAWeeklyEl = document.findElement(By.id("spyMAWeeklyGraph"));

			Logger.log().info("Строим различные Moving Averages и HilbertTrend для тикера SPY");

			String spyMADailyElText = createMAGraphics("SPY", 2018, 2019,
					Frequency.Daily, MarketIndicatorInterval.Daily);
			
			Logger.log().info("Текст для отображежения графика MA для SPY (daily): " + spyMADailyElText);

			
			String spyMAWeeklyElText = createMAGraphics("SPY", 2016, 2019,
					Frequency.Weekly, MarketIndicatorInterval.Weekly);

			// String spyMADailyElText = "";
			// String spyMAWeeklyElText = "";

			Logger.log().info("Текст для отображежения графика MA для SPY (weekly): " + spyMAWeeklyElText);

			
			spyMADailyEl.setInnerHTML(spyMADailyElText);
			spyMAWeeklyEl.setInnerHTML(spyMAWeeklyElText);
					
			JSFunction cliclMABtnJSFun = browser.executeJavaScriptAndReturnValue("clicklOpenAllBtn").asFunction();

			Logger.log().info("Вызов функции JS - " + cliclMABtnJSFun);

			cliclMABtnJSFun.invoke(null);
			
			browser.loadHTML(browser.getHTML());
			
			isCalcSpyMovingAverageGraphics = true;
		} else
			Logger.log().info("Данные для отображения графиков графиков MA для SPY уже были загружены");

	}
	
	private String createMAGraphics(String ticker, int startYear, int endYear, 
			Frequency frequency, MarketIndicatorInterval interval ) {
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
				"spy/moving averages(50-200) chart", "dates", ticker);
	}
}
