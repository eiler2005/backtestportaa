package ru.backtesting.signal;

import java.time.LocalDateTime;

import ru.backtesting.stockindicators.StockIndicatorsHistory;
import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.utils.Logger;

public class SMATechIndicatorSignal implements SignalTestingAction {
	private int timePeriod1, timePeriod2 = 0;
	
	
	public SMATechIndicatorSignal(int timePeriod1, int timePeriod2) {
		super();
		this.timePeriod1 = timePeriod1;
		this.timePeriod2 = timePeriod2;
	}

	public SMATechIndicatorSignal(int timePeriod) {
		super();
		this.timePeriod1 = timePeriod;
	}

	
	@Override
	public int testSignal(LocalDateTime date, String ticker) {
		StockIndicatorsHistory.storage().fillSMAData(ticker, timePeriod1);
		
		if (timePeriod2 != 0) {
			StockIndicatorsHistory.storage().fillSMAData(ticker, timePeriod2);

			double value1 = StockIndicatorsHistory.storage().findIndicatorValue(ticker, timePeriod1, date, StockIndicatorsHistory.SMA_IND_ID);
			double value2 = StockIndicatorsHistory.storage().findIndicatorValue(ticker, timePeriod2, date, StockIndicatorsHistory.SMA_IND_ID);
			
			Logger.log().info("Sma[" + timePeriod1 + "] on date [" + date + "]: ticker [" + ticker + "], sma = " + Logger.log().doubleLog(value1));
			Logger.log().info("Sma[" + timePeriod2 + "] on date [" + date + "]: ticker [" + ticker + "], sma = " + Logger.log().doubleLog(value2));

			
			// for example, sma50 > sma200 - buy signal
			if (value1 > value2) {
				if (timePeriod1 == 50 && timePeriod2 == 200 )
					Logger.log().info("Бычий рынок, т.к. Sma[50] > Sma[200]");
				
				return 1;
			}
			// for example, sma200 > sma50 - sell signal
			if (value2 > value1) {
				if (timePeriod1 == 50 && timePeriod2 == 200 )
					Logger.log().info("Медвежий рынок, т.к. Sma[50] < Sma[200]");
				
				return -1;
			}
			else
				return 0;
		}
		else {
			double smaValue = StockIndicatorsHistory.storage().findIndicatorValue(ticker, timePeriod1, date, StockIndicatorsHistory.SMA_IND_ID);
			
			double quote = StockQuoteHistory.storage().getQuoteValueByDate(ticker, date, false);
			
			Logger.log().info("Sma[" + timePeriod1 + "] on date [" + date + "]: ticker [" + ticker + "] quote = " + Logger.log().doubleLog(quote) 
					+ ", sma = " + Logger.log().doubleLog(smaValue));
			
			// buy signal
			if ( quote > smaValue )
				return 1;
			// sell signal
			if (quote < smaValue)
				return -1;
			else
				return 0;
		}		
	}
}
