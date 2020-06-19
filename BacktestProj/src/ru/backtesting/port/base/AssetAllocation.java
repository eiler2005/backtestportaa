package ru.backtesting.port.base;

public interface AssetAllocation {
	public String getTicker();
	
	public double getAllocationPercent();
	
	public void setAllocPercent(double allocPercent);
	
	public boolean isHoldInPort();
	
	public void holdAssetInPort();
	
	public void sellAsset();
}
