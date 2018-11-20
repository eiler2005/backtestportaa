package ru.backtesting.techindicators;

import java.time.LocalDateTime;

import ru.backtesting.stockquotes.StockQuoteHistory;

public class TechIndicatorsUtils {
	
	// 0 - sell, price belove the sma
	// 1 - buy 	price above the sma
	// -1 other
	public static int haveSMASignal(LocalDateTime date, String ticker, SMAType smaType) {
		double smaValue = TechIndicatorsHistory.storage().findIndicatorData(ticker, smaType, date);
		
		double quote = StockQuoteHistory.storage().getQuoteValueByDate(ticker, date, false);
		
		double addPercent = 3.0*quote / 100;
		
		System.out.println("ticker " + ticker + " quote = " + quote + ", sma = " + smaValue);
		
		if ( Math.abs(quote - smaValue) > addPercent ) {
			if ( quote > smaValue )
				return 1;
			if (quote < smaValue)
				return 0;
		}
		
		return -1;
	}
}
