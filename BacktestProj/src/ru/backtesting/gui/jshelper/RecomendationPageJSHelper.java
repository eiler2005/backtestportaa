package ru.backtesting.gui.jshelper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.JSFunction;
import com.teamdev.jxbrowser.chromium.JSValue;
import com.teamdev.jxbrowser.chromium.dom.By;
import com.teamdev.jxbrowser.chromium.dom.DOMDocument;
import com.teamdev.jxbrowser.chromium.dom.DOMElement;
import com.teamdev.jxbrowser.chromium.events.ScriptContextAdapter;
import com.teamdev.jxbrowser.chromium.events.ScriptContextEvent;

import ru.backtesting.mktindicators.BollingerBandsIndicatorSignal;
import ru.backtesting.mktindicators.HilbertTrendlineSignal;
import ru.backtesting.mktindicators.RSIOscillatorSignal;
import ru.backtesting.mktindicators.base.MarketIndicatorInterface;
import ru.backtesting.mktindicators.base.MarketIndicatorType;
import ru.backtesting.mktindicators.base.MarketIndicatorsHistory;
import ru.backtesting.mktindicators.ma.MovingAverageIndicatorSignal;
import ru.backtesting.port.MarketConstants;
import ru.backtesting.stockquotes.StockQuote;
import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.stockquotes.TradingTimeFrame;
import ru.backtesting.stockquotes.graphics.GraphicsUtils;
import ru.backtesting.stockquotes.graphics.MarketIndicatorDataSeries;
import ru.backtesting.stockquotes.graphics.MarketQuoteDataSeries;
import ru.backtesting.utils.Logger;

public class RecomendationPageJSHelper {
	private Browser browser;
	private Map<String, Boolean> isCalcDataForGraphics;
	
	public RecomendationPageJSHelper(Browser browser) {
		this.browser = browser;
		isCalcDataForGraphics = new HashMap<String, Boolean>();
	}
	
	public void loadGraphics(String ticker) {
		Logger.log().info("Асинхронный вызов метода \"loadSpyGraphics\" с аргументом: " + ticker);

		if (!isCalcDataForGraphics.containsKey(ticker)) {

		} else
			Logger.log().info("Данные для отображения графиков графиков MA, RSI, VXX и т.п. для " + ticker
					+ " уже были загружены");

		// асинхронный вызов метода
		new Thread(new Runnable() {
			public void run() {
				try {
					addMAGraphicsOnPage(browser, ticker);

					addRSIGraphicsOnPage(browser, ticker);

					addVolatilityGraphicsOnPage(browser, ticker, MarketConstants.BASE_USA_VOLATILITY_INDEX_TICKER);

					addStockIndicatorsTable(browser, ticker);

					Logger.log().info("-||Перезагрузка страницы начата||-");

					browser.loadHTML(browser.getHTML());

					Logger.log().info("-||Перезагрузка страницы завершена||-");

					isCalcDataForGraphics.put(ticker, new Boolean(true));

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	private void addMAGraphicsOnPage(Browser browser, String ticker) {
		DOMDocument document = browser.getDocument();

		DOMElement tickerMADailyEl = document.findElement(By.id(ticker + "MADailyGraph"));

		DOMElement tickerMAWeeklyEl = document.findElement(By.id(ticker + "MAWeeklyGraph"));
		
		Logger.log().info("Строим различные Moving Averages и HilbertTrend для тикера \"" + ticker + "\"");

		String tickerMADailyElText = createMAGraphics(ticker, MarketConstants.shortTermPeriod.getStartYear(), 
				MarketConstants.shortTermPeriod.getEndYear(), TradingTimeFrame.Daily);

		Logger.log().info("Текст для отображежения графика MA для тикера \"" + ticker + "\" (daily): " + tickerMADailyElText);

		String tickerMAWeeklyElText = createMAGraphics(ticker, MarketConstants.longTermPeriod.getStartYear(), 
				MarketConstants.longTermPeriod.getEndYear(), TradingTimeFrame.Weekly);

		Logger.log().info("Текст для отображежения графика MA для тикера \"" + ticker +"\" (weekly): " + tickerMAWeeklyElText);

		tickerMADailyEl.setInnerHTML(tickerMADailyElText);
		tickerMAWeeklyEl.setInnerHTML(tickerMAWeeklyElText);
		
		browser.addScriptContextListener(new ScriptContextAdapter() {
            @Override
            public void onScriptContextCreated(ScriptContextEvent event) {
                JSValue value = browser.executeJavaScriptAndReturnValue(event.getJSContext().getFrameId(),"window");
                value.asObject().setProperty("openMAPanel", true);
                
				Logger.log().info("Устанавливаем переменную \"openMAPanel\", значение: " + true);
            }
        });
	}
	
	private void addRSIGraphicsOnPage(Browser browser, String ticker) {
		DOMDocument document = browser.getDocument();

		DOMElement tickerRSIShortTermGraphEl = document.findElement(By.id(ticker + "RSIShortTermGraph"));

		DOMElement tickerRSILongTermGraphEl = document.findElement(By.id(ticker + "RSILongTermGraph"));

		Logger.log().info("Строим различные RSI(14- и 100-дневные) для тикера \"" + ticker + "\"");
		
		String tickerRSIShortTermGraphElText = createRSIGraphics(ticker, MarketConstants.shortTermPeriod.getStartYear(), 
				MarketConstants.shortTermPeriod.getEndYear(), 14, TradingTimeFrame.Daily);

		Logger.log().info("Текст для отображежения графика RSI(14) для тикера \"" + ticker + "\" (daily): " + tickerRSIShortTermGraphElText);

		String tickerRSILongTermGraphElText = createRSIGraphics(ticker, MarketConstants.longTermPeriod.getStartYear(), 
				MarketConstants.longTermPeriod.getEndYear(), 100, TradingTimeFrame.Daily);

		Logger.log().info("Текст для отображежения графика RSI(100) для тикера \"" + ticker + "\" (weekly): " + tickerRSIShortTermGraphElText);

		tickerRSIShortTermGraphEl.setInnerHTML(tickerRSIShortTermGraphElText);
		tickerRSILongTermGraphEl.setInnerHTML(tickerRSILongTermGraphElText);

		browser.addScriptContextListener(new ScriptContextAdapter() {
            @Override
            public void onScriptContextCreated(ScriptContextEvent event) {
                JSValue value = browser.executeJavaScriptAndReturnValue(event.getJSContext().getFrameId(),"window");

                value.asObject().setProperty("openRSIPanel", true);
                
				Logger.log().info("Устанавливаем переменную \"openRSIPanel\", значение: " + true);
            }
        });
	}
	
	private void addVolatilityGraphicsOnPage(Browser browser, String baseTicker, String volatilityTicker) {		
		DOMDocument document = browser.getDocument();

		DOMElement volGraphEl = document.findElement(By.id(baseTicker + "VolatalityGraph"));

		Logger.log().info("Строим график волатильности для тикера \"" + baseTicker + "\" c помощью графика "
				+ "движения цен на актив с тикером \"" + volatilityTicker + "\"");
		
		String volGraphElText = createVolatilityGraphics(baseTicker, volatilityTicker, 
				TradingTimeFrame.Daily, MarketConstants.shortTermPeriod.getStartYear(), MarketConstants.shortTermPeriod.getEndYear());
			
		Logger.log().info("Текст для отображежения графика движения цен на актив с тикером \"" + 
				volatilityTicker + "\"" + volGraphElText);
		
		volGraphEl.setInnerHTML(volGraphElText);
		
		browser.addScriptContextListener(new ScriptContextAdapter() {
            @Override
            public void onScriptContextCreated(ScriptContextEvent event) {
                JSValue value = browser.executeJavaScriptAndReturnValue(event.getJSContext().getFrameId(),"window");

                value.asObject().setProperty("openVolatalityPanel", true);
                
				Logger.log().info("Устанавливаем переменную \"openVolatalityPanel\", значение: " + true);
            }
        });
	}
	
	private void addStockIndicatorsTable(Browser browser, String ticker) {		
		Logger.log().info("Заполняем итоговую таблицу с различными Moving Averages, HilbertTrend и индикаторами RSI для тикера \"" + ticker + "\"");
		
		StockQuoteHistory stockStorage = StockQuoteHistory.storage();
		
		LocalDateTime tradingDay = stockStorage.getLastTradingDayInYear(ticker, TradingTimeFrame.Daily, MarketConstants.shortTermPeriod.getEndYear());
		
		Logger.log().info("Получили ближайший торговый день для тикера \"" + ticker + "\": " + tradingDay);

		StockQuote quote = stockStorage.getQuoteByDate(ticker, TradingTimeFrame.Daily, tradingDay);
		
		Logger.log().info("Получили значение котировки для тикера \"" + ticker + "\" в ближайший торговый день " + tradingDay + ": " + quote.getClose());
		
		Logger formatter = Logger.log();
		
		DOMDocument document = browser.getDocument();

		DOMElement tickerPriceValueEl = document.findElement(By.id(ticker + "PriceValue"));
		tickerPriceValueEl.setInnerText("Last Price: " + formatter.doubleAsString(quote.getClose()) + " на дату " + 
				formatter.dateAsString(tradingDay.toLocalDate()));
		
		DOMElement indTableBodyEl = document.findElement(By.id(ticker + "RecomIndicatorsTableBody"));
		
		String tableBodyHTML = fillStockIndicatorsHtmlTable(ticker, quote.getClose(), TradingTimeFrame.Daily, tradingDay);
		
		tableBodyHTML += fillStockIndicatorsHtmlTable(ticker, quote.getClose(), TradingTimeFrame.Weekly, tradingDay);
		
		indTableBodyEl.setInnerHTML(tableBodyHTML);
		
		DOMElement rsiOSCTableBodyEl = document.findElement(By.id(ticker + "RsiOscillatorTableBody"));
		rsiOSCTableBodyEl.setInnerHTML(fillRSIIndicatorsHtmlTable(ticker, TradingTimeFrame.Daily, tradingDay));
	}
	
	private String fillRSIIndicatorsHtmlTable(String ticker, TradingTimeFrame period, LocalDateTime tradingDay) {
		double rsi14Value = MarketIndicatorsHistory.storage().findIndicatorValue(ticker, 14, tradingDay, MarketIndicatorType.RSI_OSC, period);

		double rsi100Value = MarketIndicatorsHistory.storage().findIndicatorValue(ticker, 100, tradingDay, MarketIndicatorType.RSI_OSC, period);

		Logger.log().info("Получили значение индикатора rsi(14)(" + period + ") для тикера \"" + ticker + "\" в ближайший торговый день " + tradingDay + ": " + rsi14Value);
		Logger.log().info("Получили значение индикатора rsi(100)(" + period + ") для тикера \"" + ticker + "\" в ближайший торговый день " + tradingDay + ": " + rsi100Value);
		
		String tableRsiOscElHtml = "		<tr>\r\n" + 
		"			<td>rsi (14)</td>\r\n" + 
		"			<td>" + Logger.log().doubleAsString(rsi14Value);
		
		if ( RSIOscillatorSignal.rsiOscillatorBehaviorForGUI(14, rsi14Value) == 1 )	
			tableRsiOscElHtml += "<br><span class=\"indBuy\">перепродан/покупать</span></td>\r\n";
		else if ( RSIOscillatorSignal.rsiOscillatorBehaviorForGUI(14, rsi14Value) == -1 )	
			tableRsiOscElHtml += "<br><span class=\"indSell\">перекуплен/продавать</span></td>\r\n";
		else
			tableRsiOscElHtml += "<br><span>нейтрально</span></td>\r\n";

		tableRsiOscElHtml += "		</tr>\r\n" + 
		"		<tr>\r\n" + 
		"			<td>rsi (100)</td>\r\n" + 
		"			<td>" + Logger.log().doubleAsString(rsi100Value);
		
		if ( RSIOscillatorSignal.rsiOscillatorBehaviorForGUI(100, rsi100Value) == 1 )	
			tableRsiOscElHtml += "<br><span class=\"indBuy\">перепродан/покупать</span></td>\r\n";
		else if ( RSIOscillatorSignal.rsiOscillatorBehaviorForGUI(100, rsi100Value) == -1 )	
			tableRsiOscElHtml += "<br><span class=\"indSell\">перекуплен/продавать</span></td>\r\n";
		else
			tableRsiOscElHtml += "<br><span>нейтрально</span></td>\r\n";
		
		tableRsiOscElHtml +="		</tr>";
		
		return tableRsiOscElHtml;
	}
	
	private String fillStockIndicatorsHtmlTable(String ticker, double tickerPriceValue, TradingTimeFrame period, LocalDateTime tradingDay) {
		MarketIndicatorsHistory mktIndStorage = MarketIndicatorsHistory.storage();
		
		double sma50Value = mktIndStorage.findIndicatorValue(ticker, 50, tradingDay, MarketIndicatorType.SMA_IND, period);
		double sma200Value = mktIndStorage.findIndicatorValue(ticker, 200, tradingDay, MarketIndicatorType.SMA_IND, period);
		
		Logger.log().info("Получили значение индикатора sma50(" + period + ") для тикера \"" + ticker + "\" в ближайший торговый день " + tradingDay + ": " + sma50Value);
		Logger.log().info("Получили значение индикатора sma200(" + period + ") для тикера \"" + ticker + "\" в ближайший торговый день " + tradingDay + ": " + sma200Value);

		
		double wma50Value = mktIndStorage.findIndicatorValue(ticker, 50, tradingDay, MarketIndicatorType.WMA_IND, period);
		double wma200Value = mktIndStorage.findIndicatorValue(ticker, 200, tradingDay, MarketIndicatorType.WMA_IND, period);

		Logger.log().info("Получили значение индикатора wma50("+ period + ") для тикера \"" + ticker + "\" в ближайший торговый день " + tradingDay + ": " + wma50Value);
		Logger.log().info("Получили значение индикатора wma200(" + period +") для тикера \"" + ticker + "\" в ближайший торговый день " + tradingDay + ": " + wma200Value);
		
		double hilbertTrendValue = mktIndStorage.findIndicatorValue(ticker, 0, tradingDay, MarketIndicatorType.HILBER_TRANSFORM_TRENDLINE, period);

		Logger.log().info("Получили значение индикатора hilbert trend("+ period + ") для тикера \"" + ticker + "\" в ближайший торговый день " + tradingDay + ": " + hilbertTrendValue);

		String tableBodyHTML = 
				// --tr
				"<tr>\r\n" + 
				"					<td>MA 50 (" + period +")</td>\r\n";
		
				// td
				tableBodyHTML += generateTableDataHTML(tickerPriceValue, sma50Value);
				// td
				tableBodyHTML += generateTableDataHTML(tickerPriceValue, wma50Value);
				// td
				tableBodyHTML += generateTableDataHTMLWithPercentageDiff(tickerPriceValue, sma50Value);
				// td
				tableBodyHTML += generateTableDataHTMLWithPercentageDiff(tickerPriceValue, wma50Value);

				tableBodyHTML +="		</tr>\r\n";

				
				// --tr
				tableBodyHTML +="		</tr>\r\n" + 
				"				<tr>\r\n" + 
				"					<td>MA 200 (" + period + ")</td>\r\n";
				
				// td
				tableBodyHTML += generateTableDataHTML(tickerPriceValue, sma200Value);
				// td
				tableBodyHTML += generateTableDataHTML(tickerPriceValue, wma200Value);
				// td				
				tableBodyHTML += generateTableDataHTMLWithPercentageDiff(tickerPriceValue, sma200Value);
				// td
				tableBodyHTML += generateTableDataHTMLWithPercentageDiff(tickerPriceValue, wma200Value);

				tableBodyHTML +="		</tr>\r\n";

				
				// --tr
				tableBodyHTML +="		</tr>\r\n" + 
				"				<tr>\r\n" + 
				"					<td>Trend Line (" + period +")</td>\r\n";
				
				// td
				tableBodyHTML += generateTableDataHTML(tickerPriceValue, hilbertTrendValue);
				// td
				tableBodyHTML += generateTableDataHTML(tickerPriceValue, hilbertTrendValue);
				// td
				tableBodyHTML += generateTableDataHTMLWithPercentageDiff(tickerPriceValue, hilbertTrendValue);
				// td
				tableBodyHTML += generateTableDataHTMLWithPercentageDiff(tickerPriceValue, hilbertTrendValue);
				tableBodyHTML +="		</tr>\r\n";
		
		return tableBodyHTML;
	}
	
	private String generateTableDataHTML(double tickerPriceValue, double indValue) {
		Logger formatter = Logger.log();

		String buyHtml = "<br><span class=\"indBuy\">покупать</span>";
		String sellHtml = "<br><span class=\"indSell\">продавать</span>";
		
		String html = "					<td>" + formatter.doubleAsString(indValue);
		
		if ( tickerPriceValue >= indValue )
			html += buyHtml + "</td>\r\n";
		else
			html += sellHtml + "</td>\r\n";
		
		return html;
	}
	
	private String generateTableDataHTMLWithPercentageDiff(double tickerPriceValue, double indValue) {
		Logger formatter = Logger.log();

		String buyHtml = "<br><span class=\"indBuy\">покупать</span>";
		String sellHtml = "<br><span class=\"indSell\">продавать</span>";
		
		double indToPricePercentageDiff = percent(tickerPriceValue, indValue);
		
		String html = "";
		
		if ( indToPricePercentageDiff >= 0  )
			html += "	<td>" + formatter.doubleAsString(indToPricePercentageDiff) + "%" + buyHtml + "</td>\r\n";
		else 
			html += "	<td>" + formatter.doubleAsString(indToPricePercentageDiff) + "%" + sellHtml + "</td>\r\n";
		
		return html;
	}
	
	private String createRSIGraphics(String ticker, int startYear, int endYear, int timePeriod, TradingTimeFrame period) {
		MarketIndicatorInterface rsiOsc = new RSIOscillatorSignal(timePeriod, period);

		MarketQuoteDataSeries quotesDataSeries = new MarketQuoteDataSeries(ticker, startYear, endYear, period, false);
		
		MarketIndicatorDataSeries rsiIndDataSeries = new MarketIndicatorDataSeries(ticker, rsiOsc, 
				"rsi(" + timePeriod + ") : " + ticker, startYear, endYear, period);
				
		return GraphicsUtils.createOverlayTimeSeriesChart(quotesDataSeries, rsiIndDataSeries, 
				ticker + "/rsi(" + timePeriod + ") chart", "dates");
	}
	
	private String createVolatilityGraphics(String baseStockTicker, String volatilityTicker, TradingTimeFrame period, int startYear, int endYear) {		
		MarketQuoteDataSeries quotesDataSeries1 = new MarketQuoteDataSeries(baseStockTicker, startYear, endYear, period, false);
		
		MarketQuoteDataSeries quotesDataSeries2 = new MarketQuoteDataSeries(volatilityTicker, startYear, endYear, period, false);

		return GraphicsUtils.createOverlayTimeSeriesChart(quotesDataSeries1, quotesDataSeries2, 
				baseStockTicker + "/" + volatilityTicker + "chart", "dates");
	}
	
	private String createMAGraphics(String ticker, int startYear, int endYear, TradingTimeFrame period ) {
		MarketQuoteDataSeries quotesDataSeries = new MarketQuoteDataSeries(ticker, startYear, endYear, period, false);
		
		MarketIndicatorInterface sma50 = new MovingAverageIndicatorSignal(50, MarketIndicatorType.SMA_IND, period, 1);
		MarketIndicatorInterface sma200 = new MovingAverageIndicatorSignal(200, MarketIndicatorType.SMA_IND, period, 1);

		MarketIndicatorDataSeries sma50DataSeries = new MarketIndicatorDataSeries(ticker, sma50, 
				sma50.getMarketIndType() + "(" + sma50.getTimePeriod() + ") : " + ticker, startYear, endYear, period);
		
		MarketIndicatorDataSeries sma200DataSeries = new MarketIndicatorDataSeries(ticker, sma200, 
				sma200.getMarketIndType() + "(" + sma200.getTimePeriod() + ") : " + ticker, startYear, endYear, period);
		
		MarketIndicatorInterface wma50 = new MovingAverageIndicatorSignal(50, MarketIndicatorType.WMA_IND, period, 1);
		MarketIndicatorDataSeries wma50DataSeries = new MarketIndicatorDataSeries(ticker, wma50, 
				wma50.getMarketIndType() + "(" + wma50.getTimePeriod() + ") : " + ticker, startYear, endYear, period);
		
		MarketIndicatorInterface wma200 = new MovingAverageIndicatorSignal(200, MarketIndicatorType.WMA_IND, period, 1);
		MarketIndicatorDataSeries wma200DataSeries = new MarketIndicatorDataSeries(ticker, wma200, 
				wma200.getMarketIndType() + "(" + wma200.getTimePeriod() + ") : " + ticker, startYear, endYear, period);
		
		MarketIndicatorInterface hilbertTrend = new HilbertTrendlineSignal(1, period);
		MarketIndicatorDataSeries hilbertTrendDataSeries = new MarketIndicatorDataSeries(ticker, hilbertTrend, 
				hilbertTrend.getMarketIndType() + "(none) : " + ticker, startYear, endYear, period);
		
		MarketIndicatorInterface bb50 = new BollingerBandsIndicatorSignal(50, period);
		MarketIndicatorDataSeries bb50DataSeries = new MarketIndicatorDataSeries(ticker, bb50, 
				bb50.getMarketIndType() + "(" + bb50.getTimePeriod() + ") : " + ticker, startYear, endYear, period);
		
		MarketIndicatorInterface bb200 = new BollingerBandsIndicatorSignal(200, period);
		MarketIndicatorDataSeries bb200DataSeries = new MarketIndicatorDataSeries(ticker, bb200, 
				bb200.getMarketIndType() + "(" + bb200.getTimePeriod() + ") : " + ticker, startYear, endYear, period);
		
		return GraphicsUtils.createMultipleTimeSeriesChart(
				Arrays.asList(new MarketQuoteDataSeries[] { quotesDataSeries}), 
				Arrays.asList(new MarketIndicatorDataSeries[] { sma50DataSeries, sma200DataSeries, 
						wma50DataSeries, wma200DataSeries, hilbertTrendDataSeries/*, bb50DataSeries, bb200DataSeries*/}), 
				ticker + "/moving averages(50-200) chart", "dates", ticker);
	}
	
	@SuppressWarnings("unused")
	private void callJSFunction(Browser browser) {
		JSFunction cliclMABtnJSFun = browser.executeJavaScriptAndReturnValue("clicklOpenAllBtn").asFunction();

		Logger.log().info("Вызов функции JS - " + cliclMABtnJSFun);

		cliclMABtnJSFun.invoke(null);
	}
	
	private double percent(double part, double total) {
		return (part - total) / total * 100.0d;
	}
}