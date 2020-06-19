package ru.backtesting.port.base.aa.momentum;

import java.time.LocalDateTime;

import ru.backtesting.port.base.AllocChoiceModelType;
import ru.backtesting.port.base.aa.AssetAllocPerfInf;
import ru.backtesting.port.base.aa.AssetInThePort;

public class MomAssetAllocPerfInf implements AssetAllocPerfInf {
	protected String ticker;
	
	protected LocalDateTime startDate, endDate;
	
	protected double allocPercent = 0;
	
	protected double stockQuoteStart, stockQuoteEnd;

	protected double percGrowth;
	
	protected AssetInThePort isHoldInPort = AssetInThePort.Sell;
	
	public MomAssetAllocPerfInf(String ticker, LocalDateTime startDate, LocalDateTime endDate,
			double stockQuoteStart, double stockQuoteEnd, double percGrowth) {
		super();
		this.ticker = ticker;
		this.startDate = startDate;
		this.endDate = endDate;
		this.stockQuoteStart = stockQuoteStart;
		this.stockQuoteEnd = stockQuoteEnd;
		this.percGrowth = percGrowth;
	}

	@Override
	public String getTicker() {
		return ticker;
	}

	@Override
	public LocalDateTime getStartDate() {
		return startDate;
	}

	public LocalDateTime getEndDate() {
		return endDate;
	}

	public double getPercGrowth() {
		return percGrowth;
	}

	@Override
	public double getStockQuoteStart() {
		return stockQuoteStart;
	}

	public double getStockQuoteEnd() {
		return stockQuoteEnd;
	}

	@Override
	public AllocChoiceModelType getType() {
		return AllocChoiceModelType.Momentum;
	}

	@Override
	public void setAllocPercent(double allocPercent) {
		this.allocPercent = allocPercent;
	}

	@Override
	public boolean isHoldInPort() {
		if ( isHoldInPort == AssetInThePort.Hold )
			return true;
		else return false;
	}

	@Override
	public void holdAssetInPort( ) {
		isHoldInPort = AssetInThePort.Hold;		
	}

	@Override
	public void sellAsset() {
		isHoldInPort = AssetInThePort.Sell;	
		
		allocPercent = 0;
	}
	
	@Override
	public double getAllocationPercent() {
		return allocPercent;
	}
	
	@Override
	public String toString() {
		return "MomAssetAllocPerfInf [ticker=" + ticker + ", allocPercent=" + allocPercent + ", percGrowth = " + percGrowth + ", type = " + getType() + "]";
	}
}
