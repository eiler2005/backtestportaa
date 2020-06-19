package ru.backtesting.port.results;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;

import ru.backtesting.mktindicators.ma.MovingAverageIndicatorSignal;
import ru.backtesting.port.Portfolio;
import ru.backtesting.port.PositionInformation;
import ru.backtesting.port.base.AllocChoiceModelType;
import ru.backtesting.port.base.TimingModel;
import ru.backtesting.port.base.aa.AssetAllocPerfInf;
import ru.backtesting.port.base.aa.momentum.MomAssetAllocPerfInf;
import ru.backtesting.port.base.aa.sma.MovingAverageAssetAllocInf;
import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.stockquotes.TradingTimeFrame;
import ru.backtesting.utils.Logger;
import tech.tablesaw.api.BooleanColumn;
import tech.tablesaw.api.DateColumn;
import tech.tablesaw.api.DateTimeColumn;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;

public class LaunchResults {
	private LocalDateTime launchTime;
	private Portfolio portfilio;

	private Table monthlyRetunsTable;
	private Table riskOnOffTable;
	private Table riskOnOffDetailedTable;
	private Table rabalancedTable;

	private Table timingAssetsTable;
	private Table perfAssetsTable;

	private Table stockQuoteTable;
	private Table stockQuantityTable;

	private Table detailedAssetsiInfTable;
	
	private int startYear, endYear;

	private Map<String, Object> paramsMap;
	private Map<LocalDateTime, Boolean> riskResults;

	public LaunchResults(LocalDateTime launchTime, Portfolio portfilio) {
		super();
		this.launchTime = launchTime;
		this.portfilio = portfilio;

		riskResults = new HashMap<LocalDateTime, Boolean>();

		basePortParams(portfilio.getName(), portfilio.getPvzLink(), portfilio.getStartYear(), portfilio.getEndYear(),
				portfilio.getInitialAmount(), portfilio.getTimingModel().getOutOfMarketPosTicker(),
				portfilio.isReinvestDividends());
	}

	public LocalDateTime getLaunchTime() {
		return launchTime;
	}

	public Portfolio getPortfolio() {
		return portfilio;
	}

	private void basePortParams(String portName, String pvzLink, int startYear, int endYear, double initialBalance,
			String outOfMarketPosTicker, boolean reinvestDividents) {
		Map<String, Object> portParamsMap = new LinkedHashMap<String, Object>();

		portParamsMap.put("portfolio name", portName);
		
		if ( pvzLink != null )
			portParamsMap.put("pvz link", pvzLink);
		else
			portParamsMap.put("pvz link", "");

		portParamsMap.put("initial balance", new Double(initialBalance));
		portParamsMap.put("reinvest dividends", new Boolean(reinvestDividents));
		portParamsMap.put("out of market ticker", outOfMarketPosTicker);

		this.startYear = startYear;
		this.endYear = endYear;

		paramsMap = portParamsMap;
	}

	public void putDetailedRiskOnOffInformation(LocalDateTime date, List<AssetAllocPerfInf> assetAllocPerfInfList) {
		boolean riskResult = riskResults.get(date).booleanValue();
		
		BooleanColumn riskResColumn = BooleanColumn.create("result", 
				Arrays.asList(new Boolean[] { new Boolean(riskResult) }));
		
		List<Column<?>> assInfColumnList = new ArrayList<>();
		
		assInfColumnList.add(riskResColumn);
		
		DateColumn date1Column = null, date2Column = null;
		
		for (AssetAllocPerfInf inf : assetAllocPerfInfList) {			
			LocalDate date1 = inf.getStartDate().toLocalDate();
			
			date1Column = DateColumn.create("date1",
					Arrays.asList(new LocalDate[] { date1 }));
			
			String ticker = inf.getTicker();

			double stockQuote1 = inf.getStockQuoteStart();

			DoubleColumn quote1Column = DoubleColumn.create(ticker + " date1",
					Arrays.asList(new Double[] { new Double(stockQuote1) }));
			
			DoubleColumn quote2Column = null;
			
			if (inf.getType() == AllocChoiceModelType.Momentum && inf instanceof MomAssetAllocPerfInf) {			
				LocalDate date2 = ((MomAssetAllocPerfInf)inf).getEndDate().toLocalDate();
		
				double perfPercent = ((MomAssetAllocPerfInf)inf).getPercGrowth();
				
				double stockQuote2 = ((MomAssetAllocPerfInf)inf).getStockQuoteEnd();
				
				DoubleColumn tickerPerfColumn = DoubleColumn.create(ticker + " (perf)",
						Arrays.asList(new Double[] { new Double(perfPercent) }));
		
				date2Column = DateColumn.create("date2",
						Arrays.asList(new LocalDate[] { date2 }));
				
				quote2Column = DoubleColumn.create(ticker + " date2",
						Arrays.asList(new Double[] { new Double(stockQuote2) }));
				
				assInfColumnList.add(tickerPerfColumn);
			}
			
			assInfColumnList.add(quote1Column);
			
			if ( quote2Column != null )
				assInfColumnList.add(quote2Column);
		}
		
		riskOnOffDetailedTable = appendColumns("risk on/off (perf in %, stock quote)", riskOnOffDetailedTable, 
			assInfColumnList.toArray(new Column<?>[assInfColumnList.size()]));
		
		if ( date2Column != null )
			rabalancedTable = appendColumns("rebalanced", rabalancedTable, date1Column, date2Column);
		else
			rabalancedTable = appendColumns("rebalanced", rabalancedTable, date1Column);
	}
	
	public void putDetailedBacktestInformation(List<AssetAllocPerfInf> assetAllocPerfInfList) {
		List<Column<?>> asetsAllocColumnList = fillTimingAssetsTable(assetAllocPerfInfList);
		
		timingAssetsTable = appendColumns("timing assets (asset/allocation in %)", timingAssetsTable, 
				asetsAllocColumnList.toArray(new Column<?>[asetsAllocColumnList.size()]));
		
		List<Column<?>> perfColumnList = fillPerfAssetsTable(assetAllocPerfInfList);
		
		if ( perfColumnList.size() != 0 )
			perfAssetsTable = appendColumns("performance (in %)", perfAssetsTable, 
					perfColumnList.toArray(new Column<?>[perfColumnList.size()]));
		
		List<Column<?>> stockQuoteColumnList = fillStockQuoteTable(assetAllocPerfInfList);
		
		stockQuoteTable = appendColumns("stock quote", stockQuoteTable, 
				stockQuoteColumnList.toArray(new Column<?>[stockQuoteColumnList.size()]));
		
		List<Column<?>> detailedBackTestColumnList = fillDetailedAssetsiInfTable(assetAllocPerfInfList);
		
		detailedAssetsiInfTable = appendColumns("timing assets (asset/allocation in %, perf and quote", detailedAssetsiInfTable, 
				detailedBackTestColumnList.toArray(new Column<?>[detailedBackTestColumnList.size()]));
	}

	private List<Column<?>> fillDetailedAssetsiInfTable(List<AssetAllocPerfInf> assetAllocPerfInfList) {
		List<Column<?>> assInfColumnList = new ArrayList<>();
		
		for (AssetAllocPerfInf inf : assetAllocPerfInfList) {						
			String ticker = inf.getTicker();

			DoubleColumn allocColumn = DoubleColumn.create(ticker + " (aa,%)",
					Arrays.asList(new Double[] { new Double(inf.getAllocationPercent()) }));
			
			assInfColumnList.add(allocColumn);
			
			double stockQuote1 = inf.getStockQuoteStart();

			DoubleColumn quote1Column = DoubleColumn.create(ticker + " date1",
					Arrays.asList(new Double[] { new Double(stockQuote1) }));
			
			DoubleColumn quote2Column = null;
			
			if (inf.getType() == AllocChoiceModelType.Momentum && inf instanceof MomAssetAllocPerfInf) {		
				double perfPercent = ((MomAssetAllocPerfInf)inf).getPercGrowth();
				
				double stockQuote2 = ((MomAssetAllocPerfInf)inf).getStockQuoteEnd();
				
				DoubleColumn tickerPerfColumn = DoubleColumn.create(ticker + " (perf,%)",
						Arrays.asList(new Double[] { new Double(perfPercent) }));
				
				quote2Column = DoubleColumn.create(ticker + " date2",
						Arrays.asList(new Double[] { new Double(stockQuote2) }));
				
				assInfColumnList.add(tickerPerfColumn);
			}
			
			assInfColumnList.add(quote1Column);
			
			if ( quote2Column != null )
				assInfColumnList.add(quote2Column);
		}
		
		return assInfColumnList;
	}

	
	private List<Column<?>> fillStockQuoteTable(List<AssetAllocPerfInf> assetAllocPerfInfList) {
		List<Column<?>> quoteColumnList = new ArrayList<>();
		
		for (AssetAllocPerfInf inf : assetAllocPerfInfList) {
			String ticker = inf.getTicker();

			if (inf.getType() == AllocChoiceModelType.Momentum && inf instanceof MomAssetAllocPerfInf) {			
				double quoteValue = ((MomAssetAllocPerfInf)inf).getStockQuoteEnd();
				
				DoubleColumn quoteColumn = DoubleColumn.create(ticker,
						Arrays.asList(new Double[] { new Double(quoteValue) }));
				
				quoteColumnList.add(quoteColumn);
			} else {
				throw new NotImplementedException("Метод еще не реализован для других типов timing моделей");
			}
		}
		
		return quoteColumnList;
	}

	
	private List<Column<?>> fillPerfAssetsTable(List<AssetAllocPerfInf> assetAllocPerfInfList) {
		List<Column<?>> perfColumnList = new ArrayList<>();
		
		for (AssetAllocPerfInf inf : assetAllocPerfInfList) {
			String ticker = inf.getTicker();

			if (inf.getType() == AllocChoiceModelType.Momentum && inf instanceof MomAssetAllocPerfInf) {			
				double perfPercent = ((MomAssetAllocPerfInf)inf).getPercGrowth();
				
				DoubleColumn perfColumn = DoubleColumn.create(ticker,
						Arrays.asList(new Double[] { new Double(perfPercent) }));
				
				perfColumnList.add(perfColumn);
			}			
		}
		
		return perfColumnList;
	}

	
	private List<Column<?>> fillTimingAssetsTable(List<AssetAllocPerfInf> assetAllocPerfInfList) {
		// asset alloc table
		List<Column<?>> asetsAllocColumnList = new ArrayList<>();

		for (AssetAllocPerfInf inf : assetAllocPerfInfList) {
			String ticker = inf.getTicker();

			double allocPersent = inf.getAllocationPercent();

			DoubleColumn allocColumn = DoubleColumn.create(ticker,
					Arrays.asList(new Double[] { new Double(allocPersent) }));

			asetsAllocColumnList.add(allocColumn);
		}

		return asetsAllocColumnList;
	}
	
	public void putRiskOnOffInfForMovingAverage(LocalDateTime date, boolean riskValue, 
			MovingAverageAssetAllocInf inf) {
		BooleanColumn riskResColumn = BooleanColumn.create("result",
				Arrays.asList(new Boolean[] { new Boolean(riskValue) }));

		DoubleColumn signalColumn = DoubleColumn.create(inf.getTicker(),
				Arrays.asList(new Double[] { new Double(inf.getStockQuoteEnd()) }));

		String smaSignalHeader;
		
		MovingAverageIndicatorSignal signal = inf.getSmaSignal();
		
		if ( signal.havingAdditaionalTimePeriod() )
			smaSignalHeader = "ma (" + signal.getTimePeriod() + " / " + signal.getAdditionalTimePeriod() + ")";
		else 
			smaSignalHeader = "ma (" + signal.getTimePeriod() + ")";
		
		DoubleColumn indColumn = DoubleColumn.create(smaSignalHeader,
				Arrays.asList(new Double[] { new Double(signal.getIndValue()) }));

		riskOnOffTable = appendColumns("risk on/off (performance in %)", riskOnOffTable, riskResColumn, signalColumn,
				indColumn);

		riskResults.put(date, new Boolean(riskValue));
	}
	
	public void putRiskOnOffInfForAbsMom(LocalDateTime date, boolean riskValue, double absMomAssetInf, double cashFundInf) {
		BooleanColumn riskResColumn = BooleanColumn.create("result",
				Arrays.asList(new Boolean[] { new Boolean(riskValue) }));

		DoubleColumn absMomColumn = DoubleColumn.create("abs mom asset",
				Arrays.asList(new Double[] { new Double(absMomAssetInf) }));

		DoubleColumn cashFundColumn = DoubleColumn.create("cash fund",
				Arrays.asList(new Double[] { new Double(cashFundInf) }));

		riskOnOffTable = appendColumns("risk on/off (performance in %)", riskOnOffTable, riskResColumn, absMomColumn,
				cashFundColumn);

		riskResults.put(date, new Boolean(riskValue));
	}

	public void putPortBalanceOnDate(LocalDateTime date, double balance) {
		DateTimeColumn dateColumn = DateTimeColumn.create("date", Arrays.asList(new LocalDateTime[] { date }));

		DoubleColumn balanceColumn = DoubleColumn.create("balance",
				Arrays.asList(new Double[] { new Double(balance) }));

		monthlyRetunsTable = appendColumns("monthly returns", monthlyRetunsTable, dateColumn, balanceColumn);
	}

	public void putStockQuantityInPort(TimingModel timingModel,
			LinkedHashMap<LocalDateTime, List<PositionInformation>> postionsOnDates) {
		List<String> tickers = timingModel.getPortTickers();

		tickers.add(timingModel.getOutOfMarketPosTicker());
		
		for (LocalDateTime curDate : postionsOnDates.keySet()) {
			List<Column<?>> columnList = new ArrayList<>();
			
			for (String ticker : tickers) {

				double quantityStock = 0;

				for (PositionInformation position : postionsOnDates.get(curDate)) {
					if (position.getTicker().equalsIgnoreCase(ticker))
						quantityStock = position.getQuantity();
				}
				
				DoubleColumn quantityColumn = DoubleColumn.create(ticker,
						Arrays.asList(new Double[] { new Double(quantityStock) }));

				columnList.add(quantityColumn);
			}
			
			Logger.log().info(columnList.toString());
			
			stockQuantityTable = appendColumns("stock quantity", stockQuantityTable,
					columnList.toArray(new Column<?>[columnList.size()]));
		}
	}
	
	public Table getRiskOnOffDetailedTable() {
		return riskOnOffDetailedTable;
	}

	public Table getMonthlyRetunsTable() {
		return monthlyRetunsTable;
	}

	public Table getRiskOnOffTable() {
		return riskOnOffTable;
	}

	public Map<String, Object> getBasePortParams() {
		return paramsMap;
	}

	public int getStartYear() {
		return startYear;
	}

	public int getEndYear() {
		return endYear;
	}

	public Table getRabalancedTable() {
		return rabalancedTable;
	}

	public Table getTimingAssetsTable() {
		return timingAssetsTable;
	}
	
	public Table getPerfAssetsTable() {
		return perfAssetsTable;
	}

	public Table getStockQuoteTable() {
		return stockQuoteTable;
	}

	public Table getStockQuantityTable() {
		return stockQuantityTable;
	}
	

	public Table getDetailedAssetsiInfTable() {
		return detailedAssetsiInfTable;
	}

	private Table appendColumns(String tableHeader, Table table, final Column<?>... cols) {
		if (table == null) {
			table = Table.create(tableHeader);

			table.addColumns(cols);
		} else
			table = table.append(Table.create(tableHeader).addColumns(cols));

		return table;
	}
}
