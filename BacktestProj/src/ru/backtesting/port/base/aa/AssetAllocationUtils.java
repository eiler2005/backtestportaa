package ru.backtesting.port.base.aa;

import java.util.List;

import ru.backtesting.port.base.AllocationType;
import ru.backtesting.port.base.AssetAllocation;
import ru.backtesting.port.base.ticker.TickerInf;

public class AssetAllocationUtils {

	public static void sellAssetsInPortWithCore(List<? extends AssetAllocation> assets, List<? extends AssetAllocation> assetsAllocEtalon) {
		for(AssetAllocation asset : assets) {
			boolean sell = true;
			
			for(AssetAllocation etAlloc : assetsAllocEtalon)
				if ( etAlloc.getTickerInf().equals(asset.getTickerInf()) && 
						etAlloc.getType().equals(AllocationType.Core) ) {
					sell = false;
						
					asset.setAllocPercent(etAlloc.getAllocationPercent());
				}
				
			if (sell )
				asset.sellAsset();
			else
				asset.holdAssetInPort();
		}
	}

	public static void sellAssetsInPort(List<AssetAllocPerfInf> assets) {
		for(AssetAllocPerfInf asset : assets)
			asset.sellAsset();
	}

	public static double calcSummarizedAllocPercentWithCore(List<? extends AssetAllocation> assetsAlloc) {
		double sum = 0;
		
		for (AssetAllocation aa : assetsAlloc)
			if ( aa.getType().equals(AllocationType.Core) )
				sum += aa.getAllocationPercent();
		
		return sum;
	}

	public static double calcSummarizedAllocPercent(List<? extends AssetAllocation> assetsAlloc) {
		double sum = 0;
		
		for (AssetAllocation aa : assetsAlloc)
			sum += aa.getAllocationPercent();
		
		return sum;
	}

	public static void setAllocPercentByTicker(TickerInf ticker, double allocPercent, List<AssetAllocPerfInf> allocList) {
		for(AssetAllocPerfInf alloc : allocList)
			if ( alloc.getTickerInf().equals(ticker) )
				alloc.setAllocPercent(allocPercent);
	}

}
