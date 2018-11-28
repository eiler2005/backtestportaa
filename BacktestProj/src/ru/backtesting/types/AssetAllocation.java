package ru.backtesting.types;

public class AssetAllocation {
	private String ticker;
	private double allocPercent;
	
	public AssetAllocation(String ticker, double allocation) {
		super();
		this.ticker = ticker;
		this.allocPercent = allocation;
	}
	
	public String getTicker() {
		return ticker;
	}
	public double getAllocationPercent() {
		return allocPercent;
	}

	@Override
	public String toString() {
		return "AssetAllocation [ticker=" + ticker + ", allocPercent=" + allocPercent + "]";
	}
}
