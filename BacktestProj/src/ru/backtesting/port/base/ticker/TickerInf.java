package ru.backtesting.port.base.ticker;

public interface TickerInf {
	public String getTicker();

	public String getTickerId();
	
	public void generateId();	
}
