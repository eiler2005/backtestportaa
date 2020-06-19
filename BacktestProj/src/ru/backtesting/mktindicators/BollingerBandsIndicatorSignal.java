package ru.backtesting.mktindicators;

import java.time.LocalDateTime;

import org.apache.commons.lang3.NotImplementedException;

import ru.backtesting.mktindicators.base.MarketIndicatorInterface;
import ru.backtesting.mktindicators.base.MarketIndicatorType;
import ru.backtesting.mktindicators.base.MarketIndicatorsHistory;
import ru.backtesting.stockquotes.TradingTimeFrame;
import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.utils.Logger;

public class BollingerBandsIndicatorSignal implements MarketIndicatorInterface {
	private int timePeriod;
	private TradingTimeFrame interval;
	
	public BollingerBandsIndicatorSignal(int timePeriod, TradingTimeFrame interval) {
		this.timePeriod = timePeriod;
		this.interval = interval;
	}

	@Override
	public int testSignal(LocalDateTime date, String ticker) {
		MarketIndicatorsHistory.storage().fillBBandsData(ticker, timePeriod, getInterval());
		
		double bbValue = MarketIndicatorsHistory.storage().findIndicatorValue(ticker, timePeriod, 
				date, getMarketIndType(), getInterval());
		
		double stockValue = StockQuoteHistory.storage().getQuoteByDate(ticker, getInterval(), date).getClose();
		
		Logger.log().info("BOLLINGER BANDS INDICATOR[" + timePeriod + "] on date [" + date + "]: ticker [" + ticker + "] bbands = " 
			+ Logger.log().doubleAsString(bbValue));
		
		Logger.log().info("Stock value on date [" + date + "]: ticker [" + ticker + "] value = " 
				+ Logger.log().doubleAsString(stockValue));
				
		// buy signal
		if ( stockValue > bbValue )
			return 1;
		// sell signal
		else if (stockValue <= bbValue)
			return -1;
		else
			return 0;
	}

	@Override
	public MarketIndicatorType getMarketIndType() {
		return MarketIndicatorType.BOLLINGER_BANDS;
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

	@Override
	public double getIndValue() {
		throw new NotImplementedException("Метод еще не реализован для индикатора " + getMarketIndType());
	}
}
