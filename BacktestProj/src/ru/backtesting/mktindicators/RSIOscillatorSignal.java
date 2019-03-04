package ru.backtesting.mktindicators;

import java.time.LocalDateTime;

import ru.backtesting.mktindicators.base.MarketIndicatorInterface;
import ru.backtesting.mktindicators.base.MarketIndicatorType;
import ru.backtesting.mktindicators.base.MarketIndicatorsHistory;
import ru.backtesting.stockquotes.TradingTimeFrame;
import ru.backtesting.utils.Logger;

public class RSIOscillatorSignal implements MarketIndicatorInterface {
	private int timePeriod;
	private TradingTimeFrame interval;
	
	public RSIOscillatorSignal(int timePeriod, TradingTimeFrame interval) {
		this.timePeriod = timePeriod;
		this.interval = interval;
	}
	
	@Override
	public int testSignal(LocalDateTime date, String ticker) {
		MarketIndicatorsHistory.storage().fillRSIData(ticker, timePeriod, getInterval());
		
		double rsiValue = MarketIndicatorsHistory.storage().findIndicatorValue(ticker, timePeriod, date, getMarketIndType(), getInterval());
				
		Logger.log().info("RSI[" + timePeriod + "] on date [" + date + "]: ticker [" + ticker + "] rsi = " 
				+ Logger.log().doubleAsString(rsiValue));
		
		return rsiOscillatorBehavior(timePeriod, rsiValue);
	}

	private int rsiOscillatorBehavior(int timePeriod, double rsiValue) {
		if (timePeriod >= 80) {
			// sell signal
			if (rsiValue >= 70)
				return -1;
			
			// buy signal
			if ( rsiValue <= 32 )
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
	
	public static int rsiOscillatorBehaviorForGUI(int timePeriod, double rsiValue) {
		if (timePeriod >= 80) {
			// sell signal
			if (rsiValue >= 70)
				return -1;
			
			// buy signal
			if ( rsiValue <= 32 )
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
		if ( timePeriod >= 100 && rsiValue >= 50  )
			return 1;
		// sell signal
		else if ( timePeriod >= 100 && rsiValue < 50  )
			return -1;
		else
			return 0;
	}
	
	@Override
	public MarketIndicatorType getMarketIndType() {
		return MarketIndicatorType.RSI_OSC;
	}
	
	@Override
	public int getTimePeriod() {
		return timePeriod;
	}

	@Override
	public int getAdditionalTimePeriod() {
		throw new UnsupportedOperationException("Additional period was not supported for indicator " + getMarketIndType());
	}
	
	@Override
	public TradingTimeFrame getInterval() {
		return interval;
	}
}
