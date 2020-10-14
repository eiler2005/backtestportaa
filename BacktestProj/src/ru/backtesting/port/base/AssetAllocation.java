package ru.backtesting.port.base;

import ru.backtesting.port.base.ticker.TickerInf;

public interface AssetAllocation extends TickerInf {	
	public double getAllocationPercent();
	
	public void setAllocPercent(double allocPercent);
	
	public boolean isHoldInPort();
	
	public void holdAssetInPort();
	
	public void sellAsset();		
	
	public TickerInf getTickerInf();
	
	public AllocationType getType();
}
