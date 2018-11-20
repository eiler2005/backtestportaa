package ru.backtesting.techindicators.test;

import java.time.LocalDateTime;

import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.techindicators.SMAType;
import ru.backtesting.techindicators.TechIndicatorsHistory;
import ru.backtesting.techindicators.TechIndicatorsUtils;

public class TechIndicatorsTest {
	public static void main(String[] args) {		
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
