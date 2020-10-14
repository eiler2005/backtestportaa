package ru.backtesting.port.base.aa.momentum;

import java.time.LocalDateTime;

import ru.backtesting.port.base.AllocChoiceModelType;
import ru.backtesting.port.base.AllocationType;
import ru.backtesting.port.base.aa.AssetAllocPerfInf;
import ru.backtesting.port.base.aa.AssetInThePort;
import ru.backtesting.port.base.ticker.AbstractTickerInf;
import ru.backtesting.port.base.ticker.TickerInf;

public class MomAssetAllocPerfInf extends AbstractTickerInf implements AssetAllocPerfInf {
	
	protected LocalDateTime startDate, endDate;
	
	protected double allocPercent = 0;
	
	protected double stockQuoteStart, stockQuoteEnd;

	protected double percGrowth;
	
	protected AssetInThePort isHoldInPort = AssetInThePort.Sell;
	
	private String code;
	
	public MomAssetAllocPerfInf(String ticker, LocalDateTime startDate, LocalDateTime endDate,
			double stockQuoteStart, double stockQuoteEnd, double percGrowth) {
		super(ticker);
		
		this.startDate = startDate;
		this.endDate = endDate;
		this.stockQuoteStart = stockQuoteStart;
		this.stockQuoteEnd = stockQuoteEnd;
		this.percGrowth = percGrowth;
	}
	
	public MomAssetAllocPerfInf(TickerInf tickerInf, LocalDateTime startDate, LocalDateTime endDate,
			double stockQuoteStart, double stockQuoteEnd, double percGrowth) {
		super(tickerInf);
		
		this.startDate = startDate;
		this.endDate = endDate;
		this.stockQuoteStart = stockQuoteStart;
		this.stockQuoteEnd = stockQuoteEnd;
		this.percGrowth = percGrowth;
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
	public AllocChoiceModelType getAllocModelType() {
		return AllocChoiceModelType.Momentum;
	}

	@Override
	public void setAllocPercent(double allocPercent) {
		this.allocPercent = allocPercent;
	}

	@Override
	public boolean isHoldInPort() {
		if ( percGrowth == DualMomUtils.NOT_AVAILABLE_QUOTE_PERF )
			return false;
					
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
		return "MomAssetAllocPerfInf [ticker=" + getTicker() + ", code = " + code + ", allocPercent=" + allocPercent + ", percGrowth = " + percGrowth + ", type = " + getAllocModelType() + "]";
	}

	@Override
	public AllocationType getType() {
		return AllocationType.Satellite;
	}
}
