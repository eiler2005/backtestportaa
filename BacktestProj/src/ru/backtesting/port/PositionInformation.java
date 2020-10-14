package ru.backtesting.port;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ru.backtesting.port.base.ticker.Ticker;
import ru.backtesting.port.base.ticker.TickerInf;
import ru.backtesting.utils.Logger;

public class PositionInformation implements Cloneable {
	private TickerInf tickerInf;
	private LocalDateTime time;
	private double quantity;
	private double price;
	
	public PositionInformation(TickerInf ticker, LocalDateTime time) {
		super();
		this.tickerInf = ticker;
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

	public TickerInf getTickerInf() {
		return tickerInf;
	}

	public LocalDateTime getTime() {
		return time;
	}
	
	public double getPrice() {
		return price;
	}

	@Override
	public String toString() {
		return "PositionInformation [tickerCode =" + tickerInf + ", time=" + time + ", quantity=" + Logger.log().doubleAsString(quantity) + ", price="
				+ Logger.log().doubleAsString(price) + "]";
	}
	
	public static List<TickerInf> getTickers(Collection<PositionInformation> positions) {
		List<TickerInf> posList = new ArrayList<TickerInf>();
		
		for (PositionInformation pos : positions)
			posList.add(pos.getTickerInf());

		return posList;
	}
	
	public static PositionInformation findPosByTickers(Collection<PositionInformation> positions, TickerInf ticker) {		
		for (PositionInformation pos : positions)
			if ( pos.getTickerInf().getTicker().equals(ticker.getTicker()) )
				return pos;

		return null;
	}
	
	public static PositionInformation cashPosition() {
		return new PositionInformation(Ticker.cash(), 
				LocalDateTime.now());
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		PositionInformation clonePos = new PositionInformation(this.tickerInf, this.time);
		
		clonePos.price = price;
		
		clonePos.quantity = quantity;
		
		return clonePos;
	}
}
