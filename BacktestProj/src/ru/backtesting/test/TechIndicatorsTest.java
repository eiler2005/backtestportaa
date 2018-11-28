package ru.backtesting.test;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.patriques.TechnicalIndicators;
import org.patriques.input.technicalindicators.Interval;
import org.patriques.input.technicalindicators.SeriesType;
import org.patriques.input.technicalindicators.TimePeriod;
import org.patriques.output.technicalindicators.RSI;
import org.patriques.output.technicalindicators.TechnicalIndicatorResponse;
import org.patriques.output.technicalindicators.data.IndicatorData;

import ru.backtesting.stockindicators.SMAType;
import ru.backtesting.stockindicators.StockIndicatorsHistory;
import ru.backtesting.stockindicators.StockIndicatorsUtils;
import ru.backtesting.stockquotes.StockConnector;
import ru.backtesting.stockquotes.StockQuoteHistory;

public class TechIndicatorsTest {
	public static void main(String[] args) {		
		// smaTest();
		rsiTest();
	}
	
	public static void rsiTest() {
		String ticker = "spy";
		
		StockQuoteHistory.storage().fillQuotesData(ticker, 2016, 2018);
				
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
			StockQuoteHistory.storage().getQuoteValueByDate(ticker,  data.getDateTime(), false));
	}
	
	public static void smaTest( ) {		
		StockQuoteHistory.storage().fillQuotesData("VOO", 2000, 2018);
		StockIndicatorsHistory.storage().fillSMAData("VOO", 200);
		
		// Data:276.3265, 2018-10-26T00:00
		LocalDateTime date1 = LocalDateTime.parse("2018-10-25T00:00:00");
		// Data:275.7896, 2018-11-16T00:00
		LocalDateTime date2 = LocalDateTime.parse("2018-11-16T00:00:00");

		System.out.println("sma 1: " + StockIndicatorsHistory.storage().findIndicatorValue("VOO", 200, date1, StockIndicatorsHistory.SMA_IND_ID));
		System.out.println("sma 2: " + StockIndicatorsHistory.storage().findIndicatorValue("VOO", 200, date2, StockIndicatorsHistory.SMA_IND_ID));
		
		StockQuoteHistory.storage().fillQuotesData("MTUM", 2000, 2018);
		StockIndicatorsHistory.storage().fillSMAData("MTUM", 200);

		
		System.out.println(StockIndicatorsUtils.haveSMASignal(date2, "VOO", 200));
		
		System.out.println(StockIndicatorsUtils.haveSMASignal(date2, "MTUM", 200));
	}
}
