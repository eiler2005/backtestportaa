package ru.backtesting.signal;

import java.time.LocalDateTime;

import ru.backtesting.stockindicators.StockIndicatorsHistory;
import ru.backtesting.utils.Logger;

@Deprecated
public class ChandeMomentumOscillator implements SignalTestingAction {
	private int timePeriod;
	
	public ChandeMomentumOscillator(int timePeriod) {
		this.timePeriod = timePeriod;
	}
	
	@Override
	public int testSignal(LocalDateTime date, String ticker) {
		StockIndicatorsHistory.storage().fillCMOData(ticker, timePeriod);
		
		double cmoValue = StockIndicatorsHistory.storage().findIndicatorValue(ticker, timePeriod, date, StockIndicatorsHistory.CHANDE_MOMENTUM_OSC_ID);
				
		Logger.log().info("Chande Momentum Oscillator[" + timePeriod + "] on date [" + date + "]: ticker [" + ticker + "] cmo value = " 
				+ Logger.log().doubleLog(cmoValue));
		
		// нужно считать sma по осциллятору и входить если выше sma и выше 50
		
		// buy signal
		if ( cmoValue <= -50 )
			return 1;
		// sell signal
		if (cmoValue >= 50)
			return -1;
		else
			return 0;
	}

}
