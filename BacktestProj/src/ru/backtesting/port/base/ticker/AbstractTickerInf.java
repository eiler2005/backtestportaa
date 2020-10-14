package ru.backtesting.port.base.ticker;

public abstract class AbstractTickerInf implements TickerInf {
	private TickerInf ticker;

	public AbstractTickerInf(String tickerStr) {
		super();
		this.ticker = new Ticker(tickerStr);
	}
	
	public AbstractTickerInf(TickerInf tickerInf) {
		super();
		this.ticker = tickerInf;
	}
	
	@Override
	public String getTicker() {
		return ticker.getTicker();
	}
	
	@Override
	public String getTickerId() {
		return ticker.getTickerId();
	}

	@Override
	public void generateId() {
		ticker.generateId();
	}
	
	public TickerInf getTickerInf() {
		return ticker;
	}
}

