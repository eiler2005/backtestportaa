package ru.backtesting.mktindicators;

import java.time.LocalDateTime;

import org.apache.commons.lang3.NotImplementedException;

import ru.backtesting.mktindicators.base.MarketIndicatorInterface;
import ru.backtesting.mktindicators.base.MarketIndicatorType;
import ru.backtesting.mktindicators.base.MarketIndicatorsHistory;
import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.stockquotes.TradingTimeFrame;
import ru.backtesting.utils.Logger;

public class HilbertTrendlineSignal implements MarketIndicatorInterface {
	private double deviationPercent;
	private TradingTimeFrame interval;
	
	public HilbertTrendlineSignal(double deviationPercent, TradingTimeFrame interval) {
		super();
		this.deviationPercent = deviationPercent / 100;
		this.interval = interval;
	}

	@Override
	public int testSignal(LocalDateTime date, String ticker) {
		MarketIndicatorsHistory.storage().fillHilbertTrendlineData(ticker, interval);

		double trendValue = MarketIndicatorsHistory.storage().findIndicatorValue(ticker, 0, date, getMarketIndType(), getInterval());

		Logger.log().info("HilbertTrendline value on date [" + date + "]: ticker [" + ticker + "] = " 
				+ Logger.log().doubleAsString(trendValue));

		double quoteValue = StockQuoteHistory.storage().getQuoteByDate(ticker, getInterval(), date).getClose();

		
		// buy signal
		if ( quoteValue > (trendValue + trendValue*deviationPercent) )
			return 1;
		// sell signal
		if (quoteValue < (trendValue - trendValue*deviationPercent) )
			return -1;
		else
			return 0;
	}

	@Override
	public MarketIndicatorType getMarketIndType() {
		return MarketIndicatorType.HILBER_TRANSFORM_TRENDLINE;
	}

	@Override
	public int getTimePeriod() {
		return 0;
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
