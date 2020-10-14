package ru.backtesting.port.base.aa.momentum;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.backtesting.port.base.AllocChoiceModelType;
import ru.backtesting.port.base.AssetAllocation;
import ru.backtesting.port.base.aa.AssetAllocPerfInf;
import ru.backtesting.port.base.aa.AssetAllocationChoiceModel;
import ru.backtesting.port.base.aa.AssetAllocationUtils;
import ru.backtesting.port.base.ticker.Ticker;
import ru.backtesting.port.base.ticker.TickerInf;
import ru.backtesting.port.results.BacktestResultsStorage;
import ru.backtesting.utils.Logger;

public class DualMomentumAllocModel implements AssetAllocationChoiceModel {
	private String absMomAssetTicker = "SPY";
	private String moneyFundTicker = "TIP";
	
	// by default 2
	private int monthPerfPeriod = -1;
	
	// by default 1-4
	private int assetsHoldCount = -1;	
	
	// String moneyFundTicker = "shv";
	// String moneyFundTicker = "tip"; c 2005ого года

	// String moneyFundTicker = "bnd";
	// String moneyFundTicker = "BIL"; - котировки корявые, c 2008ого
	
	public DualMomentumAllocModel(String absMomAssetTicker, String moneyFundTicker, int monthPerfPeriod,
			int assetsHoldCount) {
		super();
		this.absMomAssetTicker = absMomAssetTicker;
		this.moneyFundTicker = moneyFundTicker;
		this.monthPerfPeriod = monthPerfPeriod;
		this.assetsHoldCount = assetsHoldCount;
	}
	
	@Override
	public List<AssetAllocPerfInf> calculateAllocation(LocalDateTime date, List<? extends AssetAllocation> assetsAllocEtalon, List<TickerInf> tickers, 
			TickerInf outOfMarketPos, LocalDateTime launchDate) {		
		double absMomPerfGrowth1 = DualMomUtils.calcPerformanceScoreInPercentsToMonths(date, absMomAssetTicker, monthPerfPeriod);
		
		Logger.log().info("По активу [" + absMomAssetTicker + "] на дату " + date + " процент роста за период " + monthPerfPeriod + " месяцев составил: " + 
				Logger.log().doubleAsString(absMomPerfGrowth1)  + " %");
		
		double moneyFundPercGrowth2 = DualMomUtils.calcPerformanceScoreInPercentsToMonths(date, moneyFundTicker, monthPerfPeriod);
		
		Logger.log().info("По активу [" + moneyFundTicker + "] на дату " + date + " процент роста за период " + monthPerfPeriod + " месяцев составил: " + 
				Logger.log().doubleAsString(moneyFundPercGrowth2) + " %");		
		
		boolean riskResult = false;
		
		List<AssetAllocPerfInf> detailedAssAllocInfList = new ArrayList<AssetAllocPerfInf>();
		
		// for excel table data
		detailedAssAllocInfList.addAll(DualMomUtils.getAssetAllocInfListForPerfomance(
				Ticker.createTickersList(Arrays.asList(new String[] { absMomAssetTicker, moneyFundTicker })), 
				date, 2, monthPerfPeriod, 100, false));
		AssetAllocationUtils.sellAssetsInPort(detailedAssAllocInfList);
				
		// risk on
		if ( absMomPerfGrowth1 >= moneyFundPercGrowth2  ) {
			Logger.log().info("Включаем risk on: " + absMomAssetTicker + " вырос больше " + moneyFundTicker);		
			
			riskResult = true;
			
			List<AssetAllocPerfInf> riskOnAlloc = DualMomUtils.calcRiskOnAssets(date, assetsAllocEtalon, 
					tickers, outOfMarketPos, assetsHoldCount, monthPerfPeriod);
			
			detailedAssAllocInfList.addAll(riskOnAlloc);
		// risk off
		} else {			
			Logger.log().info("Включаем risk of: " + moneyFundTicker + " вырос больше " + absMomAssetTicker);
			
			List<AssetAllocPerfInf> riskOffAlloc = DualMomUtils.calcRiskOffAssets(date, assetsAllocEtalon, tickers, outOfMarketPos, monthPerfPeriod);
			
			detailedAssAllocInfList.addAll(riskOffAlloc);
		}		
				
		// risk on/off table
		BacktestResultsStorage.getInstance().putRiskOnOffInfForAbsMom(launchDate, date, 
				riskResult, absMomPerfGrowth1, moneyFundPercGrowth2);
		
		return detailedAssAllocInfList;
	}

	@Override
	public AllocChoiceModelType getType() {
		return AllocChoiceModelType.Momentum;
	}

	@Override
	public List<String> getRiskOnOffTickers() {
		return Arrays.asList(absMomAssetTicker, moneyFundTicker);
	}

	@Override
	public Map<String, Object> getExportModelParams() {
		Map<String, Object> portParamsMap = new LinkedHashMap<String, Object>();
		
		portParamsMap.put("abs momentum asset", absMomAssetTicker);
		portParamsMap.put("money fund", moneyFundTicker);
		
		if ( assetsHoldCount != -1 )
			portParamsMap.put("assets to hold", new Double(assetsHoldCount));

		if ( monthPerfPeriod != -1 ) {
			portParamsMap.put("absolute-momentum period (AMP)", new Double(monthPerfPeriod));
			portParamsMap.put("relative-strength period (RSP)", new Double(monthPerfPeriod));
		}
		
		return portParamsMap;
	}
}
