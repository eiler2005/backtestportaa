package ru.backtesting.types;

import java.time.LocalDateTime;

public class PositionInformation {
	private String ticker;
	private LocalDateTime time;
	private double quantity;
	private double price;
	private boolean hold;
	
	public PositionInformation(String ticker, LocalDateTime time) {
		super();
		this.ticker = ticker;
		this.quantity = 0;
		this.time = time;
		this.price = 0;
		hold = true;
	}

	public double getQuantity() {
		return quantity;
	}

	public void buy(double quantity, double price) {
		this.quantity = quantity;
		this.price = price;
		hold = true;
	}

	public void sell () {
		this.quantity = 0;
		this.price = 0;
		hold = false;
	}

	public String getTicker() {
		return ticker;
	}

	public LocalDateTime getTime() {
		return time;
	}
	
	public double getPrice() {
		return price;
	}

	public boolean isHoldInPortfolio() {
		return hold;
	}
}
