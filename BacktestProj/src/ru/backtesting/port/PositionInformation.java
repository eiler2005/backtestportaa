package ru.backtesting.port;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.backtesting.utils.Logger;

public class PositionInformation {
	private String ticker;
	private LocalDateTime time;
	private double quantity;
	private double price;
	
	public PositionInformation(String ticker, LocalDateTime time) {
		super();
		this.ticker = ticker;
		this.quantity = 0;
		this.time = time;
		this.price = 0;
	}

	public double getQuantity() {
		return quantity;
	}

	public void buy(double quantity, double price) {
		this.quantity = quantity;
		this.price = price;
	}

	public void sell () {
		this.quantity = 0;
		this.price = 0;
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

	@Override
	public String toString() {
		return "PositionInformation [ticker=" + ticker + ", time=" + time + ", quantity=" + Logger.log().doubleAsString(quantity) + ", price="
				+ Logger.log().doubleAsString(price) + "]";
	}
	
	public static List<String> getTickers(List<PositionInformation> positions) {
		List<String> posList = new ArrayList<String>();
		
		for (PositionInformation pos : positions)
			posList.add(pos.getTicker());
		
		return posList;
	}
}
