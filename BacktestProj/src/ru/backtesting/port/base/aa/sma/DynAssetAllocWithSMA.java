package ru.backtesting.port.base.aa.sma;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.backtesting.mktindicators.ma.MovingAverageIndicatorSignal;
import ru.backtesting.port.base.AllocChoiceModelType;
import ru.backtesting.port.base.AssetAllocation;
import ru.backtesting.port.base.aa.AssetAllocPerfInf;
import ru.backtesting.port.base.aa.AssetAllocationChoiceModel;
import ru.backtesting.port.base.aa.AssetAllocationUtils;
import ru.backtesting.port.base.aa.momentum.DualMomUtils;
import ru.backtesting.port.base.aa.momentum.MomAssetAllocPerfInf;
import ru.backtesting.port.base.ticker.Ticker;
import ru.backtesting.port.base.ticker.TickerInf;
import ru.backtesting.port.results.BacktestResultsStorage;
import ru.backtesting.utils.Logger;

public class DynAssetAllocWithSMA implements AssetAllocationChoiceModel {
	private String signalAsset = "SPY";
	private MovingAverageIndicatorSignal smaSignal;
	
	// by default 2
	private int monthPerfPeriod = -1;
	// by default 1-4
	private int assetsHoldCount = -1;
	
	public DynAssetAllocWithSMA(String signalTicker, MovingAverageIndicatorSignal signal) {
		this.smaSignal = signal;
		this.signalAsset = signalTicker;				
	}

	public DynAssetAllocWithSMA(String signalTicker, MovingAverageIndicatorSignal signal, int monthPerfPeriod, int assetsHoldCount) {
		this.smaSignal = signal;
		this.signalAsset = signalTicker;
		
		this.monthPerfPeriod = monthPerfPeriod;
		
		this.assetsHoldCount = assetsHoldCount;
	}
	
	@Override
	public List<AssetAllocPerfInf> calculateAllocation(LocalDateTime date, List<? extends AssetAllocation> assetsAllocEtalon, List<TickerInf> tickers, 
			TickerInf outOfMarketPos, LocalDateTime launchDate) {
		if ( monthPerfPeriod == -1 )
			monthPerfPeriod = 1;
		
		int riskIndicator = smaSignal.testSignal(date, signalAsset);
				
		boolean riskResult = false;

		List<AssetAllocPerfInf> detailedAssAllocInfList = new ArrayList<AssetAllocPerfInf>();

		AssetAllocPerfInf signalAssetInf = DualMomUtils.getAssetAllocInfListForPerfomance(
				Ticker.createTickersList(signalAsset), date, 1, monthPerfPeriod, 100, false).get(0);
		
		// for excel table data
		detailedAssAllocInfList.add(signalAssetInf);
		AssetAllocationUtils.sellAssetsInPort(detailedAssAllocInfList);
				
		MovingAverageAssetAllocInf maAssetInf = new MovingAverageAssetAllocInf(signalAsset, date, 
				((MomAssetAllocPerfInf)signalAssetInf).getStockQuoteEnd(), smaSignal);;
		
				
		// risk on
		if ( riskIndicator == 1 ) {
			Logger.log().info("Включаем risk on: " + signalAsset + " выше скользяей средней на дату " + date);
						
			riskResult = true;	
									
			List<AssetAllocPerfInf> riskOnAlloc = DualMomUtils.calcRiskOnAssets(date, assetsAllocEtalon, 
					tickers, outOfMarketPos, assetsHoldCount, monthPerfPeriod);
			
			detailedAssAllocInfList.addAll(riskOnAlloc);
		} 
		// risk off
		else {
			Logger.log().info("Включаем risk off: " + signalAsset + " ниже скользяей средней  на дату " + date);
			Logger.log().info("Уходим у hedge-актив: " + outOfMarketPos);
						
			riskResult = false;
			
			List<AssetAllocPerfInf> riskOffAlloc = DualMomUtils.calcRiskOffAssets(date, assetsAllocEtalon, 
					tickers, outOfMarketPos, monthPerfPeriod);
			
			detailedAssAllocInfList.addAll(riskOffAlloc);
		}
		
		// risk on/off table
		BacktestResultsStorage.getInstance().putRiskOnOffInfForMovingAverage(launchDate, date, 
				riskResult, maAssetInf);
		
		return detailedAssAllocInfList;
	}

	@Override
	public AllocChoiceModelType getType() {
		return AllocChoiceModelType.MovingAveragesForAsset;
	}
	
	@Override
	public List<String> getRiskOnOffTickers() {
		return Arrays.asList(signalAsset);
	}
	
	@Override
	public Map<String, Object> getExportModelParams() {
		Map<String, Object> portParamsMap = new LinkedHashMap<String, Object>();
		
		portParamsMap.put("specify signal asset", signalAsset);
		portParamsMap.put("moving average type", smaSignal.getMarketIndType());
		
		if ( smaSignal.havingAdditaionalTimePeriod() )
			portParamsMap.put("moving average period", smaSignal.getTimePeriod() + " / " + smaSignal.getAdditionalTimePeriod());
		else 
			portParamsMap.put("moving average period", smaSignal.getTimePeriod());
		
		if ( assetsHoldCount != -1 )
			portParamsMap.put("assets to hold", new Double(assetsHoldCount));
		
		if ( monthPerfPeriod != -1 )
			portParamsMap.put("relative-strength period (RSP)", new Double(monthPerfPeriod));
		
		
		return portParamsMap;
	}	
}
