package ru.backtesting.test;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.patriques.input.technicalindicators.Interval;
import org.patriques.input.technicalindicators.SeriesType;
import org.patriques.input.technicalindicators.TimePeriod;
import org.patriques.output.technicalindicators.TechnicalIndicatorResponse;
import org.patriques.output.technicalindicators.data.IndicatorData;

import ru.backtesting.mktindicators.base.MarketIndicatorType;
import ru.backtesting.mktindicators.base.MarketIndicatorsHistory;
import ru.backtesting.mktindicators.utils.StockIndicatorsUtils;
import ru.backtesting.stockquotes.StockConnector;
import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.stockquotes.TradingPeriod;

public class TechIndicatorsTest {
	public static void main(String[] args) {		
		smaTest();
		// rsiTest();
	}
	
	public static void rsiTest() {
		String ticker = "spy";
		
		StockQuoteHistory.storage().fillQuotesData(ticker, TradingPeriod.Daily);
				
		TechnicalIndicatorResponse resp = StockConnector.rsi(ticker, Interval.DAILY, TimePeriod.of(14), SeriesType.CLOSE);
		Map<String, String> metaData = resp.getMetaData();
		System.out.println("indicator metadata:" + metaData);
		List<IndicatorData> indicatorData = resp.getData();
		 
		Collections.reverse(indicatorData);
		
		for (IndicatorData data : indicatorData)
			if (data.getDateTime().getYear() == 2018 && (data.getDateTime().getMonth() == Month.NOVEMBER || 
					data.getDateTime().getMonth() == Month.OCTOBER || data.getDateTime().getMonth() == Month.SEPTEMBER || 
					data.getDateTime().getMonth() == Month.AUGUST || data.getDateTime().getMonth() == Month.JULY) )
				System.out.println("indicator data["+ data.getDateTime() + "]: " + data.getData() + ", " + ticker + ":" + 
			StockQuoteHistory.storage().getQuoteValueByDate(ticker,  TradingPeriod.Daily, data.getDateTime(), false));
	}
	
	public static void smaTest( ) {		
		StockQuoteHistory.storage().fillQuotesData("VOO", TradingPeriod.Daily);
		MarketIndicatorsHistory.storage().fillIndicatosData("VOO", 200, MarketIndicatorType.SMA_IND, TradingPeriod.Daily);
		
		// Data:276.3265, 2018-10-26T00:00
		LocalDateTime date1 = LocalDateTime.parse("2018-10-25T00:00:00");
		// Data:275.7896, 2018-11-16T00:00
		LocalDateTime date2 = LocalDateTime.parse("2018-11-16T00:00:00");

		System.out.println("sma 1: " + MarketIndicatorsHistory.storage().findIndicatorValue("VOO", 200, date1, MarketIndicatorType.SMA_IND, TradingPeriod.Daily));
		System.out.println("sma 2: " + MarketIndicatorsHistory.storage().findIndicatorValue("VOO", 200, date2, MarketIndicatorType.SMA_IND, TradingPeriod.Daily));
		
		StockQuoteHistory.storage().fillQuotesData("MTUM", TradingPeriod.Daily);
		MarketIndicatorsHistory.storage().fillIndicatosData("MTUM", 200, MarketIndicatorType.SMA_IND, TradingPeriod.Daily);

		
		System.out.println(StockIndicatorsUtils.haveSMASignal(date2, "VOO", 200, TradingPeriod.Daily));
		
		System.out.println(StockIndicatorsUtils.haveSMASignal(date2, "MTUM", 200, TradingPeriod.Daily));
	}
}
