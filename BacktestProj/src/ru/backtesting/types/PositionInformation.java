package ru.backtesting.types;

import java.time.LocalDateTime;

public class PositionInformation {
	private String ticker;
	private LocalDateTime time;
	private double quantity;
	
	public PositionInformation(String ticker, LocalDateTime time) {
		super();
		this.ticker = ticker;
		this.quantity = 0;
		this.time = time;
	}

	public double getQuantity() {
		return quantity;
	}

	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}

	public String getTicker() {
		return ticker;
	}

	public LocalDateTime getTime() {
		return time;
	}
}
