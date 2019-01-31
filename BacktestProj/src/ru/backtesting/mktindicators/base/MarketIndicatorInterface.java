package ru.backtesting.mktindicators.base;

import java.time.LocalDateTime;

import ru.backtesting.stockquotes.TradingPeriod;

public interface MarketIndicatorInterface {
	public int testSignal(LocalDateTime date, String ticker);
	public MarketIndicatorType getMarketIndType();
	public int getTimePeriod();
	public int getAdditionalTimePeriod();
	public TradingPeriod getInterval();
}
