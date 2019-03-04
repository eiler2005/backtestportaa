package ru.backtesting.mktindicators.utils;

import java.time.LocalDateTime;

import ru.backtesting.mktindicators.base.MarketIndicatorType;
import ru.backtesting.mktindicators.base.MarketIndicatorsHistory;
import ru.backtesting.stockquotes.TradingTimeFrame;
import ru.backtesting.stockquotes.StockQuoteHistory;

@Deprecated
public class StockIndicatorsUtils {
	
	// -1 - sell, price belove the sma
	// 1 - buy 	price above the sma
	// 0 other
	public static int haveSMASignal(LocalDateTime date, String ticker, int timePeriod, TradingTimeFrame period) {
		double smaValue = MarketIndicatorsHistory.storage().findIndicatorValue(ticker, timePeriod, date, MarketIndicatorType.SMA_IND, period);
		
		double quote = StockQuoteHistory.storage().getQuoteValueByDate(ticker, period, date, false);
		
		double addPercent = 3.0*quote / 100;
		
		System.out.println("ticker " + ticker + " quote = " + quote + ", sma = " + smaValue);
		
		if ( Math.abs(quote - smaValue) > addPercent ) {
			if ( quote > smaValue )
				return 1;
			if (quote < smaValue)
				return -1;
		}
		
		return 0;
	}
}
