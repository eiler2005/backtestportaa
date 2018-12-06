package ru.backtesting.signal.ma;

import java.time.LocalDateTime;

import ru.backtesting.signal.SignalTestingAction;
import ru.backtesting.stockindicators.StockIndicatorsHistory;
import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.utils.Logger;

public class MovingAverageIndicatorSignal implements SignalTestingAction {
	private int timePeriod1, timePeriod2 = 0;
	private MovingAverageType type;
	
	public MovingAverageIndicatorSignal(int timePeriod1, int timePeriod2, MovingAverageType type ) {
		super();
		this.timePeriod1 = timePeriod1;
		this.timePeriod2 = timePeriod2;
		this.type = type;
	}

	public MovingAverageIndicatorSignal(int timePeriod, MovingAverageType type) {
		super();
		this.timePeriod1 = timePeriod;
		this.type = type;
	}

	
	@Override
	public int testSignal(LocalDateTime date, String ticker) {		
		String maId = getMovingAverigeID(type);
		
		StockIndicatorsHistory.storage().fillIndicatosData(ticker, timePeriod1, maId);
		
		if (timePeriod2 != 0) {
			StockIndicatorsHistory.storage().fillIndicatosData(ticker, timePeriod2, maId);

			double value1 = StockIndicatorsHistory.storage().findIndicatorValue(ticker, timePeriod1, date, maId);
			double value2 = StockIndicatorsHistory.storage().findIndicatorValue(ticker, timePeriod2, date, maId);
			
			Logger.log().info(maId + "[" + timePeriod1 + "] on date [" + date + "]: ticker [" + ticker + "], " + maId + " = " + Logger.log().doubleLog(value1));
			Logger.log().info(maId + "[" + timePeriod2 + "] on date [" + date + "]: ticker [" + ticker + "], " + maId +"= " + Logger.log().doubleLog(value2));

			
			// for example, sma50 > sma200 - buy signal
			if (value1 > value2) {
				if (timePeriod1 == 50 && timePeriod2 == 200 )
					Logger.log().info("Бычий рынок, т.к. " + maId + "[50] > " + maId + "[200]");
				
				return 1;
			}
			// for example, sma200 > sma50 - sell signal
			if (value2 > value1) {
				if (timePeriod1 == 50 && timePeriod2 == 200 )
					Logger.log().info("Медвежий рынок, т.к. " + maId + "[50] < " + maId + "[200]");
				return -1;
			}
			else
				return 0;
		}
		else {
			double maValue = StockIndicatorsHistory.storage().findIndicatorValue(ticker, timePeriod1, date, maId);
			
			double quote = StockQuoteHistory.storage().getQuoteValueByDate(ticker, date, false);
			
			Logger.log().info(maId + "[" + timePeriod1 + "] on date [" + date + "]: ticker [" + ticker + "] quote = " + Logger.log().doubleLog(quote) 
					+ ", " + maId + "= " + Logger.log().doubleLog(maValue));
			
			// buy signal
			if ( quote > maValue )
				return 1;
			// sell signal
			if (quote < maValue)
				return -1;
			else
				return 0;
		}		
	}
	
	private String getMovingAverigeID(MovingAverageType type) {
		if ( type.equals(MovingAverageType.Simple))
			return StockIndicatorsHistory.SMA_IND_ID;
		
		if ( type.equals(MovingAverageType.Exponential))
			return StockIndicatorsHistory.EMA_IND_ID;
		
		return StockIndicatorsHistory.WMA_IND_ID;
	}
}
