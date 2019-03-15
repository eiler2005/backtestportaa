package ru.backtesting.port;

import ru.backtesting.gui.jshelper.DatePeriodForGraphics;

public class MarketConstants {

	public static final String BASE_USA_VOLATILITY_INDEX_TICKER = "VXXB";
	public static final String BASE_USA_STOCK_INDEX_TICKER = "SPY";
	public static final String BASE_USA_LONG_TERM_BOND_TICKER = "TLT";
	
	// private static final String BASE_USA_STOCK_INDEX_TICKER = "HYG"; // hight yeald bonds
	
	public static final DatePeriodForGraphics shortTermPeriod = new DatePeriodForGraphics(2018, 2019);
	// private static final String BASE_USA_STOCK_INDEX_TICKER = "HYG"; // hight yeald bonds
	
	public static final DatePeriodForGraphics longTermPeriod = new DatePeriodForGraphics(2016, 2019);
}
