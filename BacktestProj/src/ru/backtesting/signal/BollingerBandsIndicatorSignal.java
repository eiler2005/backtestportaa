package ru.backtesting.signal;

import java.time.LocalDateTime;

import eu.verdelhan.ta4j.indicators.bollinger.BollingerBandsLowerIndicator;
import ru.backtesting.stockindicators.StockIndicatorsHistory;
import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.utils.Logger;

public class BollingerBandsIndicatorSignal implements SignalTestingAction {
	private int timePeriod;
	
	public BollingerBandsIndicatorSignal(int timePeriod) {
		this.timePeriod = timePeriod;
	}

	@Override
	public int testSignal(LocalDateTime date, String ticker) {
		StockIndicatorsHistory.storage().fillBBandsData(ticker, timePeriod);
		
		double bbValue = StockIndicatorsHistory.storage().findIndicatorValue(ticker, timePeriod, 
				date, StockIndicatorsHistory.BOLLINGER_BANDS_ID);
		
		double stockValue = StockQuoteHistory.storage().getQuoteValueByDate(ticker, date, false);
		
		Logger.log().info("BOLLINGER BANDS INDICATOR[" + timePeriod + "] on date [" + date + "]: ticker [" + ticker + "] bbands = " 
			+ Logger.log().doubleLog(bbValue));
		
		Logger.log().info("Stock value on date [" + date + "]: ticker [" + ticker + "] value = " 
				+ Logger.log().doubleLog(stockValue));
				
		// buy signal
		if ( stockValue > bbValue )
			return 1;
		// sell signal
		else if (stockValue <= bbValue)
			return -1;
		else
			return 0;
	}

}
