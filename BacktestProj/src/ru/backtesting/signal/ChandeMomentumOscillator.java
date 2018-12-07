package ru.backtesting.signal;

import java.time.LocalDateTime;
import java.util.List;

import ru.backtesting.stockindicators.StockIndicatorsHistory;
import ru.backtesting.utils.Logger;
import ru.backtesting.utils.SimpleMovingAverage;

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
				
		List<Double> sma50Values = StockIndicatorsHistory.storage()
				.getIndicatorsDataForLastPeriod(ticker, timePeriod, date, StockIndicatorsHistory.CHANDE_MOMENTUM_OSC_ID, 50);
		
		SimpleMovingAverage sma50 = new SimpleMovingAverage(50);
		sma50.addValues(sma50Values);
		
		List<Double> sma200Values = StockIndicatorsHistory.storage()
				.getIndicatorsDataForLastPeriod(ticker, timePeriod, date, StockIndicatorsHistory.CHANDE_MOMENTUM_OSC_ID, 200);
		
		SimpleMovingAverage sma200 = new SimpleMovingAverage(200);
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

}
