package ru.backtesting.mktindicators.ma;

import java.time.LocalDateTime;

import ru.backtesting.mktindicators.base.MarketIndicatorInterface;
import ru.backtesting.mktindicators.base.MarketIndicatorType;
import ru.backtesting.mktindicators.base.MarketIndicatorsHistory;
import ru.backtesting.stockquotes.TradingPeriod;
import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.utils.Logger;

public class MovingAverageIndicatorSignal implements MarketIndicatorInterface {
	private int timePeriod1, timePeriod2 = 0;
	private MarketIndicatorType maType;
	private double deviationPercent;
	private TradingPeriod interval;
	
	public MovingAverageIndicatorSignal(int timePeriod1, int timePeriod2, MarketIndicatorType maType, TradingPeriod interval) {
		super();
		this.timePeriod1 = timePeriod1;
		this.timePeriod2 = timePeriod2;
		this.maType = maType;
		this.deviationPercent = 0;
		this.interval = interval;
	}
	
	public MovingAverageIndicatorSignal(int timePeriod1, int timePeriod2, MarketIndicatorType maType, TradingPeriod interval, double deviationPercent) {
		super();
		this.timePeriod1 = timePeriod1;
		this.timePeriod2 = timePeriod2;
		this.maType = maType;
		this.deviationPercent = deviationPercent / 100;
		this.interval = interval;

	}

	public MovingAverageIndicatorSignal(int timePeriod, MarketIndicatorType maType, TradingPeriod interval, double deviationPercent) {
		super();
		this.timePeriod1 = timePeriod;
		this.maType = maType;
		this.deviationPercent = deviationPercent / 100;
		this.interval = interval;

	}

	
	@Override
	public int testSignal(LocalDateTime date, String ticker) {				
		MarketIndicatorsHistory.storage().fillIndicatosData(ticker, timePeriod1, getMarketIndType(), getInterval());
		
		if (deviationPercent != 0)
			Logger.log().info("Процент погрешности для скользящих средних (чтобы исключить ложные срабатывания): "+ 
					Logger.log().doubleAsString(deviationPercent*100) + " %");
		
		if (timePeriod2 != 0) {
			MarketIndicatorsHistory.storage().fillIndicatosData(ticker, timePeriod2, getMarketIndType(), getInterval());

			double value1 = MarketIndicatorsHistory.storage().findIndicatorValue(ticker, timePeriod1, date, getMarketIndType(), getInterval());
			double value2 = MarketIndicatorsHistory.storage().findIndicatorValue(ticker, timePeriod2, date, getMarketIndType(), getInterval());
			
			Logger.log().info(getMarketIndType() + "[" + timePeriod1 + "] on date [" + date + "]: ticker [" + ticker + "], " + getMarketIndType() + " = " + Logger.log().doubleAsString(value1));
			Logger.log().info(getMarketIndType() + "[" + timePeriod2 + "] on date [" + date + "]: ticker [" + ticker + "], " + getMarketIndType() +" = " + Logger.log().doubleAsString(value2));

			// for example, sma50 > sma200 - buy signal
			if ( value1  > (value2 + value1*deviationPercent) ) {
				if (timePeriod1 == 50 && timePeriod2 == 200 )
					Logger.log().info("Бычий рынок, т.к. " + getMarketIndType() + "[50] > " + getMarketIndType() + "[200]");
				
				return 1;
			}
			// for example, sma200 > sma50 - sell signal
			if ( value2 > (value1 + value1*deviationPercent) ) {
				if (timePeriod1 == 50 && timePeriod2 == 200 )
					Logger.log().info("Медвежий рынок, т.к. " + getMarketIndType() + "[50] < " + getMarketIndType() + "[200]");
				return -1;
			}
			else
				return 0;
		}
		else {
			double maValue = MarketIndicatorsHistory.storage().findIndicatorValue(ticker, timePeriod1, date, getMarketIndType(), getInterval());
			
			double quote = StockQuoteHistory.storage().getQuoteValueByDate(ticker, getInterval(), date, false);
			
			Logger.log().info(getMarketIndType() + "[" + timePeriod1 + "] on date [" + date + "]: ticker [" + ticker + "] quote = " + Logger.log().doubleAsString(quote) 
					+ ", " + getMarketIndType() + " = " + Logger.log().doubleAsString(maValue));
			
			// buy signal
			if ( quote > (maValue + maValue*deviationPercent) )
				return 1;
			// sell signal
			if (quote < (maValue - maValue*deviationPercent) )
				return -1;
			else
				return 0;
		}		
	}

	@Override
	public MarketIndicatorType getMarketIndType() {
		if ( maType.equals(MarketIndicatorType.SMA_IND))
			return MarketIndicatorType.SMA_IND;
		else if ( maType.equals(MarketIndicatorType.EMA_IND))
			return MarketIndicatorType.EMA_IND;
		else if ( maType.equals(MarketIndicatorType.WMA_IND))
			return MarketIndicatorType.WMA_IND;
		
		throw new RuntimeException("Некорретно указан тип для индикатора типа \"скользящая средняя\", indicator type = " 
				+ maType + ". Проверьте корректность указания типа индикатора!");
	}
	
	@Override
	public int getTimePeriod() {
		return timePeriod1;
	}

	@Override
	public int getAdditionalTimePeriod() {
		if ( timePeriod2 == 0)
			throw new UnsupportedOperationException();
		else
			return timePeriod2;
	}
	
	@Override
	public TradingPeriod getInterval() {
		return interval;
	}
}
