package ru.backtesting.signal;

import java.time.LocalDateTime;

import ru.backtesting.stockindicators.StockIndicatorsHistory;
import ru.backtesting.utils.Logger;

public class RSIOscillatorSignal implements SignalTestingAction {
	private int timePeriod;
	
	public RSIOscillatorSignal(int timePeriod) {
		this.timePeriod = timePeriod;
	}
	
	
	@Override
	public int testSignal(LocalDateTime date, String ticker) {
		StockIndicatorsHistory.storage().fillRSIData(ticker, timePeriod);
		
		double rsiValue = StockIndicatorsHistory.storage().findIndicatorValue(ticker, timePeriod, date, StockIndicatorsHistory.RSI_OSC_ID);
				
		Logger.log().info("RSI[" + timePeriod + "] on date [" + date + "]: ticker [" + ticker + "] rsi = " 
				+ Logger.log().doubleLog(rsiValue));
		
		if (timePeriod >= 80) {
			// sell signal
			if (rsiValue >= 80)
				return -1;
			
			// buy signal
			if ( rsiValue <= 32 )
				return 1;
			
			// buy signal
			if ( rsiValue >= 50 )
				return 1;
		}
		else if (timePeriod <= 30) {
			// buy signal
			if ( rsiValue <= 32 )
				return 1;
			
			// sell signal
			if (rsiValue >= 70)
				return -1;
		}
			
		// buy signal
		if ( rsiValue >= 50 )
			return 1;
		// sell signal
		if (rsiValue < 50)
			return -1;
		else
			return 0;
		
	}
}
