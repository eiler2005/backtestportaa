package ru.backtesting.techindicators.test;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Map;

import org.patriques.TechnicalIndicators;
import org.patriques.input.technicalindicators.Interval;
import org.patriques.input.technicalindicators.SeriesType;
import org.patriques.input.technicalindicators.TimePeriod;
import org.patriques.output.technicalindicators.RSI;
import org.patriques.output.technicalindicators.data.IndicatorData;

import ru.backtesting.stockquotes.StockConnector;
import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.techindicators.SMAType;
import ru.backtesting.techindicators.TechIndicatorsHistory;
import ru.backtesting.techindicators.TechIndicatorsUtils;

public class TechIndicatorsTest {
	public static void main(String[] args) {		
		// smaTest();
		rsiTest();
	}
	
	public static void rsiTest() {
		String ticker = "SPY";
		
		StockQuoteHistory.storage().fillQuotesData(ticker, 2016, 2018);
		
		TechnicalIndicators technicalIndicators = new TechnicalIndicators(StockConnector.conn());
		
		RSI resp = technicalIndicators.rsi(ticker, Interval.DAILY, TimePeriod.of(100), SeriesType.CLOSE);
		Map<String, String> metaData = resp.getMetaData();
		System.out.println("indicator metadata:" + metaData);
		List<IndicatorData> indicatorData = resp.getData();
		 
		for (IndicatorData data : indicatorData)
			if (data.getDateTime().getYear() == 2018 && data.getDateTime().getMonth() == Month.NOVEMBER)
				System.out.println("indicator data["+ data.getDateTime() + "]:" + data.getData() + ", " + ticker + ":" + 
			StockQuoteHistory.storage().getQuoteValueByDate(ticker,  data.getDateTime(), false));
	}
	
	public static void smaTest( ) {
		SMAType type = SMAType.TwoHundredDays;
		
		StockQuoteHistory.storage().fillQuotesData("VOO", 2000, 2018);
		TechIndicatorsHistory.storage().fillSMAData("VOO", type);
		
		// Data:276.3265, 2018-10-26T00:00
		LocalDateTime date1 = LocalDateTime.parse("2018-10-25T00:00:00");
		// Data:275.7896, 2018-11-16T00:00
		LocalDateTime date2 = LocalDateTime.parse("2018-11-16T00:00:00");

		System.out.println("sma 1: " + TechIndicatorsHistory.storage().findIndicatorData("VOO", type, date1));
		System.out.println("sma 2: " + TechIndicatorsHistory.storage().findIndicatorData("VOO", type, date2));
		
		StockQuoteHistory.storage().fillQuotesData("MTUM", 2000, 2018);
		TechIndicatorsHistory.storage().fillSMAData("MTUM", type);

		
		System.out.println(TechIndicatorsUtils.haveSMASignal(date2, "VOO", SMAType.TwoHundredDays));
		
		System.out.println(TechIndicatorsUtils.haveSMASignal(date2, "MTUM", SMAType.TwoHundredDays));
	}
}
