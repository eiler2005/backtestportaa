package ru.backtesting.gui.jshelper;

import java.time.LocalDateTime;
import java.util.Arrays;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.JSFunction;
import com.teamdev.jxbrowser.chromium.JSValue;
import com.teamdev.jxbrowser.chromium.dom.By;
import com.teamdev.jxbrowser.chromium.dom.DOMDocument;
import com.teamdev.jxbrowser.chromium.dom.DOMElement;
import com.teamdev.jxbrowser.chromium.events.ScriptContextAdapter;
import com.teamdev.jxbrowser.chromium.events.ScriptContextEvent;

import ru.backtesting.mktindicators.HilbertTrendlineSignal;
import ru.backtesting.mktindicators.RSIOscillatorSignal;
import ru.backtesting.mktindicators.base.MarketIndicatorInterface;
import ru.backtesting.mktindicators.base.MarketIndicatorType;
import ru.backtesting.mktindicators.base.MarketIndicatorsHistory;
import ru.backtesting.mktindicators.ma.MovingAverageIndicatorSignal;
import ru.backtesting.stockquotes.StockQuote;
import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.stockquotes.TradingPeriod;
import ru.backtesting.stockquotes.graphics.GraphicsUtils;
import ru.backtesting.stockquotes.graphics.MarketIndicatorDataSeries;
import ru.backtesting.stockquotes.graphics.MarketQuoteDataSeries;
import ru.backtesting.utils.Logger;

public class RecomendationPageJSHelper {
	private static final String BASE_USA_VOLATILITY_INDEX_TICKER = "VXXB";
	private static final String BASE_USA_STOCK_INDEX_TICKER = "TLT";
	
	private final DatePeriodForGraphics shortTermPeriod = new DatePeriodForGraphics(2018, 2019);
	private final DatePeriodForGraphics longTermPeriod = new DatePeriodForGraphics(2016, 2019);

	
	private Browser browser;
	private boolean isCalcSpyMovingAverageGraphics;
	
	public RecomendationPageJSHelper(Browser browser) {
		this.browser = browser;
		isCalcSpyMovingAverageGraphics = false;
	}
	
	public void loadSpyGraphics() {
		try {
			Logger.log().info("Вызов метода \"loadSpyGraphics\"");

			if (isCalcSpyMovingAverageGraphics == false) {
				addMAGraphicsOnPage(browser);
				
				addRSIGraphicsOnPage(browser);
				
				addVolatilityGraphicsOnPage(browser);
				
				addStockIndicatorsTable(browser);
				
				Logger.log().info("-||Перезагрузка страницы начата||-");
				
				browser.loadHTML(browser.getHTML());

				Logger.log().info("-||Перезагрузка страницы завершена||-");

				isCalcSpyMovingAverageGraphics = true;
			} else
				Logger.log().info("Данные для отображения графиков графиков MA, RSI, VXX и т.п. для SPY уже были загружены");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void addMAGraphicsOnPage(Browser browser) {
		DOMDocument document = browser.getDocument();

		DOMElement spyMADailyEl = document.findElement(By.id("spyMADailyGraph"));

		DOMElement spyMAWeeklyEl = document.findElement(By.id("spyMAWeeklyGraph"));
		
		Logger.log().info("Строим различные Moving Averages и HilbertTrend для тикера \"" + BASE_USA_STOCK_INDEX_TICKER + "\"");

		String spyMADailyElText = createMAGraphics(BASE_USA_STOCK_INDEX_TICKER, shortTermPeriod.getStartYear(), 
				shortTermPeriod.getEndYear(), TradingPeriod.Daily);

		Logger.log().info("Текст для отображежения графика MA для тикера \"" + BASE_USA_STOCK_INDEX_TICKER + "\" (daily): " + spyMADailyElText);

		String spyMAWeeklyElText = createMAGraphics(BASE_USA_STOCK_INDEX_TICKER, longTermPeriod.getStartYear(), 
				longTermPeriod.getEndYear(), TradingPeriod.Weekly);

		Logger.log().info("Текст для отображежения графика MA для тикера \"" + BASE_USA_STOCK_INDEX_TICKER +"\" (weekly): " + spyMAWeeklyElText);

		spyMADailyEl.setInnerHTML(spyMADailyElText);
		spyMAWeeklyEl.setInnerHTML(spyMAWeeklyElText);
		
		browser.addScriptContextListener(new ScriptContextAdapter() {
            @Override
            public void onScriptContextCreated(ScriptContextEvent event) {
                JSValue value = browser.executeJavaScriptAndReturnValue(event.getJSContext().getFrameId(),"window");
                value.asObject().setProperty("openMAPanel", true);
                
				Logger.log().info("Устанавливаем переменную \"openMAPanel\", значение: " + true);
            }
        });
	}
	
	private void addRSIGraphicsOnPage(Browser browser) {
		DOMDocument document = browser.getDocument();

		DOMElement spyRSIShortTermGraphEl = document.findElement(By.id("spyRSIShortTermGraph"));

		DOMElement spyRSILongTermGraphEl = document.findElement(By.id("spyRSILongTermGraph"));

		Logger.log().info("Строим различные RSI(14- и 100-дневные) для тикера \"" + BASE_USA_STOCK_INDEX_TICKER + "\"");
		
		String spyRSIShortTermGraphElText = createRSIGraphics(BASE_USA_STOCK_INDEX_TICKER, shortTermPeriod.getStartYear(), 
				shortTermPeriod.getEndYear(), 14, TradingPeriod.Daily);

		Logger.log().info("Текст для отображежения графика RSI(14) для тикера \"" + BASE_USA_STOCK_INDEX_TICKER + "\" (daily): " + spyRSIShortTermGraphElText);

		String spyRSILongTermGraphElText = createRSIGraphics(BASE_USA_STOCK_INDEX_TICKER, longTermPeriod.getStartYear(), 
				longTermPeriod.getEndYear(), 100, TradingPeriod.Daily);

		Logger.log().info("Текст для отображежения графика RSI(100) для тикера \"" + BASE_USA_STOCK_INDEX_TICKER + "\" (weekly): " + spyRSIShortTermGraphElText);

		spyRSIShortTermGraphEl.setInnerHTML(spyRSIShortTermGraphElText);
		spyRSILongTermGraphEl.setInnerHTML(spyRSILongTermGraphElText);

		browser.addScriptContextListener(new ScriptContextAdapter() {
            @Override
            public void onScriptContextCreated(ScriptContextEvent event) {
                JSValue value = browser.executeJavaScriptAndReturnValue(event.getJSContext().getFrameId(),"window");

                value.asObject().setProperty("openRSIPanel", true);
                
				Logger.log().info("Устанавливаем переменную \"openRSIPanel\", значение: " + true);
            }
        });
	}
	
	private void addVolatilityGraphicsOnPage(Browser browser) {
		String volatilityActiveTicker = BASE_USA_VOLATILITY_INDEX_TICKER; 
		
		DOMDocument document = browser.getDocument();

		DOMElement volGraphEl = document.findElement(By.id("volatalityGraph"));

		Logger.log().info("Строим график волатильности для тикера \"" + BASE_USA_STOCK_INDEX_TICKER + "\" c помощью графика "
				+ "движения цен на актив с тикером \"" + volatilityActiveTicker + "\"");
		
		String volGraphElText = createVolatilityGraphics(BASE_USA_STOCK_INDEX_TICKER, volatilityActiveTicker, 
				TradingPeriod.Daily, shortTermPeriod.getStartYear(), shortTermPeriod.getEndYear());
			
		Logger.log().info("Текст для отображежения графика движения цен на актив с тикером \"" + 
				volatilityActiveTicker + "\"" + volGraphElText);
		
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
	
	private void addStockIndicatorsTable(Browser browser) {
		String ticker = BASE_USA_STOCK_INDEX_TICKER;
		
		Logger.log().info("Заполняем итоговую таблицу с различными Moving Averages, HilbertTrend и индикаторами RSI для тикера \"" + ticker + "\"");
		
		StockQuoteHistory stockStorage = StockQuoteHistory.storage();
		
		LocalDateTime tradingDay = stockStorage.getLastTradingDayInYear(ticker, TradingPeriod.Daily, shortTermPeriod.getEndYear());
		
		Logger.log().info("Получили ближайший торговый день для тикера \"" + ticker + "\": " + tradingDay);

		StockQuote quote = stockStorage.getQuoteByDate(ticker, TradingPeriod.Daily, tradingDay);
		
		Logger.log().info("Получили значение котировки для тикера \"" + ticker + "\" в ближайший торговый день " + tradingDay + ": " + quote.getClose());
		
		Logger formatter = Logger.log();
		
		DOMDocument document = browser.getDocument();

		DOMElement tickerPriceValueEl = document.findElement(By.id("spyPriceValue"));
		tickerPriceValueEl.setInnerText("Last Price: " + formatter.doubleAsString(quote.getClose()) + " на дату " + 
				formatter.dateAsString(tradingDay.toLocalDate()));
		
		DOMElement indTableBodyEl = document.findElement(By.id("spyRecomIndicatorsTableBody"));
		
		String tableBodyHTML = fillStockIndicatorsHtmlTable(ticker, quote.getClose(), TradingPeriod.Daily, tradingDay);
		
		tableBodyHTML += fillStockIndicatorsHtmlTable(ticker, quote.getClose(), TradingPeriod.Weekly, tradingDay);
		
		indTableBodyEl.setInnerHTML(tableBodyHTML);
		
		DOMElement rsiOSCTableBodyEl = document.findElement(By.id("spyRsiOscillatorTableBody"));
		rsiOSCTableBodyEl.setInnerHTML(fillRSIIndicatorsHtmlTable(ticker, TradingPeriod.Daily, tradingDay));
	}
	
	private String fillRSIIndicatorsHtmlTable(String ticker, TradingPeriod period, LocalDateTime tradingDay) {
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
	
	private String fillStockIndicatorsHtmlTable(String ticker, double tickerPriceValue, TradingPeriod period, LocalDateTime tradingDay) {
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
	
	private String createRSIGraphics(String ticker, int startYear, int endYear, int timePeriod, TradingPeriod period) {
		MarketIndicatorInterface rsiOsc = new RSIOscillatorSignal(timePeriod, period);

		MarketQuoteDataSeries quotesDataSeries = new MarketQuoteDataSeries(ticker, startYear, endYear, period, false);
		
		MarketIndicatorDataSeries rsiIndDataSeries = new MarketIndicatorDataSeries(ticker, rsiOsc, 
				"rsi(" + timePeriod + ") : " + ticker, startYear, endYear, period);
				
		return GraphicsUtils.createOverlayTimeSeriesChart(quotesDataSeries, rsiIndDataSeries, 
				ticker + "/rsi(" + timePeriod + ") chart", "dates");
	}
	
	private String createVolatilityGraphics(String baseStockTicker, String volatilityTicker, TradingPeriod period, int startYear, int endYear) {		
		MarketQuoteDataSeries quotesDataSeries1 = new MarketQuoteDataSeries(baseStockTicker, startYear, endYear, period, false);
		
		MarketQuoteDataSeries quotesDataSeries2 = new MarketQuoteDataSeries(volatilityTicker, startYear, endYear, period, false);

		return GraphicsUtils.createOverlayTimeSeriesChart(quotesDataSeries1, quotesDataSeries2, 
				baseStockTicker + "/" + volatilityTicker + "chart", "dates");
	}
	
	private String createMAGraphics(String ticker, int startYear, int endYear, TradingPeriod period ) {
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
		
		return GraphicsUtils.createMultipleTimeSeriesChart(
				Arrays.asList(new MarketQuoteDataSeries[] { quotesDataSeries}), 
				Arrays.asList(new MarketIndicatorDataSeries[] { sma50DataSeries, sma200DataSeries, 
						wma50DataSeries, wma200DataSeries, hilbertTrendDataSeries }), 
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
	
	private final class DatePeriodForGraphics {
		private int startYear;
		private int endYear;
		
		private DatePeriodForGraphics(int startYear, int endYear) {
			super();
			this.startYear = startYear;
			this.endYear = endYear;
		}
		
		public int getStartYear() {
			return startYear;
		}
		public int getEndYear() {
			return endYear;
		}
	}
}
