package ru.backtesting.port.results;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.backtesting.port.Portfolio;
import ru.backtesting.port.PositionInformation;
import ru.backtesting.port.base.TimingModel;
import ru.backtesting.port.base.aa.AssetAllocPerfInf;
import ru.backtesting.port.base.aa.sma.MovingAverageAssetAllocInf;
import ru.backtesting.utils.Logger;
import tech.tablesaw.api.Table;

public class BacktestResultsStorage {	
	private static BacktestResultsStorage storage;
	
	private LinkedHashMap<LocalDateTime, LaunchResults> results;
	private LinkedHashMap<LocalDateTime, BacktestResultsExporter> exporters;;

	private BacktestResultsStorage() {
		results = new LinkedHashMap<LocalDateTime, LaunchResults>();
		exporters = new LinkedHashMap<LocalDateTime, BacktestResultsExporter>();
	}
	
	public static BacktestResultsStorage getInstance() {
		if (storage == null)
			storage = new BacktestResultsStorage();

		return storage;
	}
	
	public void putBasePortfolioParams(LocalDateTime launchDate, Portfolio port) {
		LaunchResults res = new LaunchResults(launchDate, port);
		
		results.put(launchDate, res);
		
		BacktestResultsExporter exporter = new BacktestResultsExporter(launchDate);
		
		exporter.addSheetHeader("Backtest portfolio results (like portfolio visualizer)");
		
		exporter.addPortParams(res.getBasePortParams(), "portfolio params");
		
		exporters.put(launchDate, exporter);
	}
	
	public void putMarketTimingModelParams(LocalDateTime launchDate, Portfolio port) {
		LaunchResults res = new LaunchResults(launchDate, port);
		
		results.put(launchDate, res);
		
		BacktestResultsExporter exporter = exporters.get(launchDate);
				
		exporter.addPortParams(port.getTimingModel().getExportModelParams(), "market timing model");
		
		exporter.addPortParams(port.getTimingModel().getRiskControlParams(), "risk control signals");
	}
	
	public void putBacktestResults(LocalDateTime launchDate, int startYear, int endYear, double cagr,
			double drawdown, double finalBalance) {
		
		Map<String, Object> portParamsMap = new LinkedHashMap<String, Object>();
		
		portParamsMap.put("cagr (%) momentum asset", cagr);
		portParamsMap.put("max drawdown",  drawdown);
		portParamsMap.put("final balance",  finalBalance);
		
		BacktestResultsExporter exporter = exporters.get(launchDate);
				
		exporter.addPortParams(portParamsMap, 
				"Market Timing Results (" + startYear + " - " + endYear + ")");
	}
	
	public void putPortBalanceOnDate(LocalDateTime launchDate, LocalDateTime date, double balance) {
		results.get(launchDate).putPortBalanceOnDate(date, balance);
	}
	
	public void putRiskOnOffInfForAbsMom(LocalDateTime launchDate, LocalDateTime date, boolean riskValue, double absMomAssetInf, double cashFundInf) {
		results.get(launchDate).putRiskOnOffInfForAbsMom(date, riskValue, absMomAssetInf, cashFundInf);
	}
	
	public void putRiskOnOffInfForMovingAverage(LocalDateTime launchDate, LocalDateTime date, boolean riskValue, 
			MovingAverageAssetAllocInf inf) {
		results.get(launchDate).putRiskOnOffInfForMovingAverage(date, riskValue, inf);
	}
	
	public void putDetailedRiskOnOffInformation(LocalDateTime launchDate, LocalDateTime date, List<AssetAllocPerfInf> assetAllocPerfInf) {
		results.get(launchDate).putDetailedRiskOnOffInformation(date, assetAllocPerfInf);
	}
	
	public void putDetailedBacktestInformation(LocalDateTime launchDate, List<AssetAllocPerfInf> assetAllocPerfInf) {
		results.get(launchDate).putDetailedBacktestInformation(assetAllocPerfInf);
	}
	
	public void putStockQuantityInPort(LocalDateTime launchDate, TimingModel timingModel, LinkedHashMap<LocalDateTime, List<PositionInformation>> postionsOnDates) {
		results.get(launchDate).putStockQuantityInPort(timingModel, postionsOnDates);
	}
	
	public void writeToFile(LocalDateTime launchDate) {
		try {
			LaunchResults res = results.get(launchDate);
						
			BacktestResultsExporter exporter = exporters.get(launchDate);
			
			Table monthlyRetunsTable = res.getMonthlyRetunsTable();

			exporter.addTableSidewaysOnMainSheet(monthlyRetunsTable.name(), monthlyRetunsTable);
			
			Table riskOnOffTable = res.getRiskOnOffTable();

			exporter.addTableSidewaysOnMainSheet(riskOnOffTable.name(), riskOnOffTable);
			
			Table rebTable = res.getRabalancedTable();

			exporter.addTableSidewaysOnDetailedSheet(rebTable.name(), rebTable);
			
			// detailed sheet
			Table riskOnOffDetailedTable = res.getRiskOnOffDetailedTable();

			exporter.addTableSidewaysOnDetailedSheet(riskOnOffDetailedTable.name(), riskOnOffDetailedTable);

			Table timingAssetsTable = res.getTimingAssetsTable();
			
			exporter.addTableSidewaysOnMainSheet(timingAssetsTable.name(), timingAssetsTable);

			Table perfAssetsTable = res.getPerfAssetsTable();

			if ( perfAssetsTable != null ) {
				exporter.addTableSidewaysOnMainSheet(perfAssetsTable.name(), perfAssetsTable);
			}
			
			Table stockQuoteTable = res.getStockQuoteTable();

			exporter.addTableSidewaysOnMainSheet(stockQuoteTable.name(), stockQuoteTable);
			
			Table stockQuantityTable = res.getStockQuantityTable();

			exporter.addTableSidewaysOnMainSheet(stockQuantityTable.name(), stockQuantityTable);
			
			// detailed sheet
			Table assetsiInfTable = res.getDetailedAssetsiInfTable();

			exporter.addTableSidewaysOnDetailedSheet(assetsiInfTable.name(), assetsiInfTable);
			
			exporter.writeToFile();
		} catch (IOException e) {
			Logger.log().error(e.getLocalizedMessage());
		}
	}
}
