package ru.backtesting.mktindicators.base;

import java.time.LocalDateTime;

public interface MarketIndicatorInterface {
	public int testSignal(LocalDateTime date, String ticker);
	public MarketIndicatorType getMarketIndType();
	public int getTimePeriod();
	public int getAdditionalTimePeriod();
	public MarketIndicatorInterval getInterval();
}
