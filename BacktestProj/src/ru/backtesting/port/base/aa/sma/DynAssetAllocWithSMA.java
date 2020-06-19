package ru.backtesting.port.base.aa.sma;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.backtesting.mktindicators.ma.MovingAverageIndicatorSignal;
import ru.backtesting.port.base.AllocChoiceModelType;
import ru.backtesting.port.base.aa.AssetAllocPerfInf;
import ru.backtesting.port.base.aa.AssetAllocationChoiceModel;
import ru.backtesting.port.base.aa.momentum.DualMomUtils;
import ru.backtesting.port.base.aa.momentum.MomAssetAllocPerfInf;
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
	public List<AssetAllocPerfInf> calculateAllocation(LocalDateTime date, List<String> tickers,
			String outOfMarketPosTicker, LocalDateTime launchDate) {
		if ( monthPerfPeriod == -1 )
			monthPerfPeriod = 1;
		
		int riskIndicator = smaSignal.testSignal(date, signalAsset);
				
		boolean riskResult = false;

		List<AssetAllocPerfInf> detailedAssAllocInfList = new ArrayList<AssetAllocPerfInf>();

		AssetAllocPerfInf signalAssetInf = DualMomUtils.getAssetAllocInfListForParams(Arrays.asList(new String[] { signalAsset}), 
				date, 1, monthPerfPeriod, false).get(0);
		
		// for excel table data
		detailedAssAllocInfList.add(signalAssetInf);
		DualMomUtils.sellAssetsInPort(detailedAssAllocInfList);
		
		MomAssetAllocPerfInf outOfMarketTickerAssetAllocInf;
		
		MovingAverageAssetAllocInf maAssetInf = new MovingAverageAssetAllocInf(signalAsset, date, 
				((MomAssetAllocPerfInf)signalAssetInf).getStockQuoteEnd(), smaSignal);;
		
		// risk on
		if ( riskIndicator == 1 ) {
			Logger.log().info("Включаем risk on: " + signalAsset + " выше скользяей средней");
						
			riskResult = true;	
			
			Map<String, Double> assetAllocEqualMap = DualMomUtils.getEquivalentAssetAllocPercent(tickers);
			
			Logger.log().info("Собираем портфель со следующими активами и аллокациями: " + assetAllocEqualMap.toString());

			if ( assetsHoldCount == -1 )
				detailedAssAllocInfList.addAll(DualMomUtils.getAssetAllocInfListForParams(
					tickers, date, tickers.size(), monthPerfPeriod, true));
			else
				detailedAssAllocInfList.addAll(DualMomUtils.getAssetAllocInfListForParams(
					tickers, date, assetsHoldCount, monthPerfPeriod, false));
			
			outOfMarketTickerAssetAllocInf = DualMomUtils.getTickerAssetAllocInf(outOfMarketPosTicker, date, 0, monthPerfPeriod);
			outOfMarketTickerAssetAllocInf.sellAsset();
			
		} 
		// risk off
		else {
			Logger.log().info("Включаем risk off: " + signalAsset + " ниже скользяей средней");
			Logger.log().info("Уходим у hedge-актив: " + outOfMarketPosTicker);
						
			riskResult = false;
			
			// данные для таблицы excel
			outOfMarketTickerAssetAllocInf = DualMomUtils.getTickerAssetAllocInf(outOfMarketPosTicker, date, 100, monthPerfPeriod);
			
			outOfMarketTickerAssetAllocInf.holdAssetInPort();
			
			detailedAssAllocInfList.addAll(DualMomUtils.getAssetAllocInfListForParams(tickers, date, tickers.size(), monthPerfPeriod, false));
			
			DualMomUtils.sellAssetsInPort(detailedAssAllocInfList);						
		}
		
		// for excel table data
		detailedAssAllocInfList.add(outOfMarketTickerAssetAllocInf);
		
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
