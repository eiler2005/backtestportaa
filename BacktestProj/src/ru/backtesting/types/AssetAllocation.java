package ru.backtesting.types;

public class AssetAllocation {
	private String ticker;
	private double allocation;
	
	public AssetAllocation(String ticker, double allocation) {
		super();
		this.ticker = ticker;
		this.allocation = allocation;
	}
	
	public String getTicker() {
		return ticker;
	}
	public double getAllocation() {
		return allocation;
	}
	
	
}
