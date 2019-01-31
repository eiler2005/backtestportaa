package ru.backtesting.mktindicators;

import java.time.LocalDateTime;
import java.util.List;

import ru.backtesting.mktindicators.base.MarketIndicatorInterface;
import ru.backtesting.mktindicators.base.MarketIndicatorType;
import ru.backtesting.mktindicators.base.MarketIndicatorsHistory;
import ru.backtesting.stockquotes.TradingPeriod;
import ru.backtesting.utils.Logger;
import ru.backtesting.utils.SMACalculator;

public class ChandeMomentumOscillator implements MarketIndicatorInterface {
	private int timePeriod;
	private TradingPeriod interval;

	public ChandeMomentumOscillator(int timePeriod, TradingPeriod interval) {
		this.timePeriod = timePeriod;
		this.interval = interval;
	}
	
	@Override
	public int testSignal(LocalDateTime date, String ticker) {
		MarketIndicatorsHistory.storage().fillCMOData(ticker, timePeriod, getInterval());
		
		double cmoValue = MarketIndicatorsHistory.storage().findIndicatorValue(ticker, timePeriod, date, getMarketIndType(), getInterval());
				
		Logger.log().info("Chande Momentum Oscillator[" + timePeriod + "] on date [" + date + "]: ticker [" + ticker + "] cmo value = " 
				+ Logger.log().doubleLog(cmoValue));
				
		List<Double> sma50Values = MarketIndicatorsHistory.storage()
				.getIndicatorsDataForLastPeriod(ticker, timePeriod, date, getMarketIndType(), getInterval(), 50);
		
		SMACalculator sma50 = new SMACalculator(50);
		sma50.addValues(sma50Values);
		
		List<Double> sma200Values = MarketIndicatorsHistory.storage()
				.getIndicatorsDataForLastPeriod(ticker, timePeriod, date, getMarketIndType(), getInterval(), 200);
		
		SMACalculator sma200 = new SMACalculator(200);
		sma200.addValues(sma200Values);
		
		double sma50Val = sma50.getMean();
		double sma200Val = sma200.getMean();
		
		Logger.log().info("sma[50] Chande Momentum Oscillator on date [" + date + "]: ticker [" + ticker + "] = " + Logger.log().doubleLog(sma50Val));
		Logger.log().info("sma[200] Chande Momentum Oscillator on date [" + date + "]: ticker [" + ticker + "] = " + Logger.log().doubleLog(sma200Val));
				
		// нужно считать sma по осциллятору и входить если выше sma и выше 50
		// for example, sma50 > sma200 - buy signal
		if (sma50Val >= sma200Val) {
			Logger.log().info("Бычий сигнал, т.к. sma[50] > sma[200]");
			
			return 1;
		}
		// for example, sma200 > sma50 - sell signal
		if (sma50Val < sma200Val) {
			Logger.log().info("Медвежий рынок, т.к. sma[50] < sma[200]");
			return -1;
		}
		else
			return 0;
		
		/*
		// buy signal
		if ( cmoValue <= -50 )
			return 1;
		// sell signal
		if (cmoValue >= 50)
			return -1;
		else
			return 0;
		*/
	}

	@Override
	public MarketIndicatorType getMarketIndType() {
		return MarketIndicatorType.CHANDE_MOMENTUM_OSC;
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
	public TradingPeriod getInterval() {
		return interval;
	}
}
