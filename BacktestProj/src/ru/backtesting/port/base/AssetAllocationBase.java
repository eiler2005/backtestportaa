package ru.backtesting.port.base;

import org.apache.commons.lang3.NotImplementedException;

import ru.backtesting.port.base.ticker.AbstractTickerInf;

public class AssetAllocationBase extends AbstractTickerInf implements AssetAllocation  {
	private double allocPercent;
	private AllocationType type;	
	
	public AssetAllocationBase(String ticker, double allocation) {
		super(ticker);
		
		this.allocPercent = allocation;
		
		this.type = AllocationType.Satellite;
	}
	
	public AssetAllocationBase(String ticker, double allocation, AllocationType type) {
		super(ticker);
		
		this.allocPercent = allocation;
		
		this.type = type;
	}
	
	public double getAllocationPercent() {
		return allocPercent;
	}

	@Override
	public String toString() {
		return "AssetAllocation [ticker=" + getTickerInf() + ", allocPercent=" + allocPercent + "]";
	}

	@Override
	public void setAllocPercent(double allocPercent) {
		this.allocPercent = allocPercent;		
	}

	@Override
	public boolean isHoldInPort() {
		throw new NotImplementedException("Метод не поддерживается для типа " + AssetAllocationBase.class.toString());
	}

	@Override
	public void holdAssetInPort() {
		throw new NotImplementedException("Метод не поддерживается для типа " + AssetAllocationBase.class.toString());
		
	}

	@Override
	public void sellAsset() {
		throw new NotImplementedException("Метод не поддерживается для типа " + AssetAllocationBase.class.toString());		
	}

	@Override
	public AllocationType getType() {
		return type;
	}
}
