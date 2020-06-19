package ru.backtesting.mktindicators.base;

import java.time.LocalDateTime;

import ru.backtesting.stockquotes.TradingTimeFrame;

public interface MarketIndicatorInterface {
	public int testSignal(LocalDateTime date, String ticker);
	public MarketIndicatorType getMarketIndType();
	public int getTimePeriod();
	public int getAdditionalTimePeriod();
	public TradingTimeFrame getInterval();
	public double getIndValue();
}
