package ru.backtesting.stockquotes;

import java.time.LocalDateTime;

public class StockQuote {
	private String ticker;
	private double open;
	private double close;
	private double high;
	private double low;
	private LocalDateTime time;
	private double dividentAmount;
	private double adjustedClose;

	
	public StockQuote(String ticker, LocalDateTime time, double open, double close, double adjustedClose, double high, double low, double dividentAmount) {
		super();
		this.ticker = ticker;
		this.open = open;
		this.close = close;
		this.adjustedClose = adjustedClose;
		this.high = high;
		this.low = low;
		this.time = time;
		this.dividentAmount = dividentAmount;
	}
	
	public LocalDateTime getTime() {
		return time;
	}
	
	public String getTicker() {
		return ticker;
	}
	public double getOpen() {
		return open;
	}
	public double getClose() {
		return close;
	}
	public double getHigh() {
		return high;
	}
	public double getLow() {
		return low;
	}

	public double getDividentAmount() {
		return dividentAmount;
	}
	
	public double getAdjustedClose() {
		return adjustedClose;
	}

	@Override
	public boolean equals(Object obj) {
		if ( obj instanceof StockQuote ) {
			StockQuote temp = (StockQuote) obj;
			
			return temp.getTicker().equals(ticker) && temp.getTime().equals(time);
		}
		else
			return super.equals(obj);
	}	
}
