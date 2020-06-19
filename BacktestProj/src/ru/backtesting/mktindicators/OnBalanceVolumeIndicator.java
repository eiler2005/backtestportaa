package ru.backtesting.mktindicators;

import java.time.LocalDateTime;

import org.apache.commons.lang3.NotImplementedException;

import ru.backtesting.mktindicators.base.MarketIndicatorInterface;
import ru.backtesting.mktindicators.base.MarketIndicatorType;
import ru.backtesting.mktindicators.base.MarketIndicatorsHistory;
import ru.backtesting.stockquotes.TradingTimeFrame;
import ru.backtesting.utils.Logger;

public class OnBalanceVolumeIndicator implements MarketIndicatorInterface {
	private TradingTimeFrame interval;
	
	public OnBalanceVolumeIndicator(TradingTimeFrame interval) {
		this.interval = interval;
	}
	
	
	@Override
	public int testSignal(LocalDateTime date, String ticker) {
		MarketIndicatorsHistory.storage().fillOBVData(ticker, getInterval());

		double rsiValue = MarketIndicatorsHistory.storage().findIndicatorValue(ticker, 0, date, getMarketIndType(), getInterval());
		
		Logger.log().info("On Balance Volume[" + getInterval() + "] on date [" + date + "]: ticker [" + ticker + "] obv = " 
				+ Logger.log().doubleAsString(rsiValue));
		
		return 1;
	}

	@Override
	public MarketIndicatorType getMarketIndType() {
		return MarketIndicatorType.OBV;
	}

	@Override
	public int getTimePeriod() {
		return 0;
	}

	@Override
	public int getAdditionalTimePeriod() {
		return 0;
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
