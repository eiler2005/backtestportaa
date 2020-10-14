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
import org.patriques.output.AlphaVantageException;

import ru.backtesting.mktindicators.ma.MovingAverageIndicatorSignal;
import ru.backtesting.port.CoreSatellitePortfolio;
import ru.backtesting.port.Portfolio;
import ru.backtesting.port.PositionInformation;
import ru.backtesting.port.PositionSet;
import ru.backtesting.port.base.AllocChoiceModelType;
import ru.backtesting.port.base.TimingModel;
import ru.backtesting.port.base.aa.AssetAllocPerfInf;
import ru.backtesting.port.base.aa.momentum.MomAssetAllocPerfInf;
import ru.backtesting.port.base.aa.sma.MovingAverageAssetAllocInf;
import ru.backtesting.port.base.ticker.TickerInf;
import ru.backtesting.port.signals.FixedLossOrProfitSignalHandler;
import ru.backtesting.port.signals.TopOverMovingAverFixedHandler;
import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.stockquotes.TradingTimeFrame;
import ru.backtesting.utils.Logger;
import ru.backtesting.utils.doubles.DeduplicateValues;
import tech.tablesaw.api.BooleanColumn;
import tech.tablesaw.api.DateColumn;
import tech.tablesaw.api.DateTimeColumn;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;

public class LaunchResults {
	private LocalDateTime launchTime;
	private Portfolio portfolio;

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
		this.portfolio = portfilio;

		riskResults = new HashMap<LocalDateTime, Boolean>();

		basePortParams(portfilio.getName(), portfilio.getPvzLink(), portfilio.getStartYear(), portfilio.getEndYear(),
				portfilio.getInitialAmount(), portfilio.getTimingModel().getOutOfMarketPosTicker().getTicker(),
				portfilio.isReinvestDividends());
	}

	public LocalDateTime getLaunchTime() {
		return launchTime;
	}

	public Portfolio getPortfolio() {
		return portfolio;
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

		if ( portfolio instanceof CoreSatellitePortfolio ) {
			CoreSatellitePortfolio coreSatPort = (CoreSatellitePortfolio) portfolio;
			
			portParamsMap.put("core allocaton (in %)",  new Double(coreSatPort.getCoreAlloc()));
			portParamsMap.put("satellite allocaton (in %)",  new Double(coreSatPort.getSatelliteAlloc()));
		}
		
		if ( portfolio.getPosSignalHandler() != null && portfolio.getPosSignalHandler() instanceof FixedLossOrProfitSignalHandler ) {
			FixedLossOrProfitSignalHandler handler = (FixedLossOrProfitSignalHandler) portfolio.getPosSignalHandler();
			
			if ( handler.getPercentPerf() >= 0 )
				portParamsMap.put("fixed profit per month (in %)", new Double(handler.getPercentPerf()));
			else
				portParamsMap.put("fixed loss per month (in %)",  new Double(handler.getPercentPerf()));
		}
		
		if ( portfolio.getPosSignalHandler() != null && portfolio.getPosSignalHandler() instanceof TopOverMovingAverFixedHandler ) {
			TopOverMovingAverFixedHandler handler = (TopOverMovingAverFixedHandler) portfolio.getPosSignalHandler();
			
			String ticker = handler.getTicker();
			
			portParamsMap.put("fixed top over moving average", "");
			portParamsMap.put("moving average type", handler.getSma().getMarketIndType());
			portParamsMap.put("moving average time period", handler.getSma().getTimePeriod());
			portParamsMap.put("ticker", ticker);
			
			if ( handler.getPercentOver() >= 0 )
				portParamsMap.put("fixed profit per month (in %)", new Double(handler.getPercentOver()));
			else
				portParamsMap.put("fixed loss per month (in %)",  new Double(handler.getPercentOver()));
		}
		
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
		
		DeduplicateValues deduplicator = new DeduplicateValues();
		
		for (AssetAllocPerfInf inf : assetAllocPerfInfList) {	
			if ( inf.getStartDate() == null )
				throw new AlphaVantageException("Не загружены котировки для тикера " + inf.getTicker() + " на дату " + 
						Logger.log().dateAsString(date.toLocalDate()) + ". Первая доступная дата для котировок: " + 
							StockQuoteHistory.storage().getFirstQuoteAvailabilityDay(inf.getTicker(), TradingTimeFrame.Daily));
			
			LocalDate date1 = inf.getStartDate().toLocalDate();
			
			date1Column = DateColumn.create("date1",
					Arrays.asList(new LocalDate[] { date1 }));
			
			String ticker = inf.getTicker();

			double stockQuote1 = inf.getStockQuoteStart();

			DoubleColumn quote1Column = DoubleColumn.create(deduplicator.check(ticker + " date1"),
					Arrays.asList(new Double[] { new Double(stockQuote1) }));
			
			DoubleColumn quote2Column = null;
			
			if (inf.getAllocModelType() == AllocChoiceModelType.Momentum && inf instanceof MomAssetAllocPerfInf) {			
				LocalDate date2 = ((MomAssetAllocPerfInf)inf).getEndDate().toLocalDate();
		
				double perfPercent = ((MomAssetAllocPerfInf)inf).getPercGrowth();
				
				double stockQuote2 = ((MomAssetAllocPerfInf)inf).getStockQuoteEnd();
				
				DoubleColumn tickerPerfColumn = DoubleColumn.create(deduplicator.check(ticker + " (perf)"),
						Arrays.asList(new Double[] { new Double(perfPercent) }));
		
				date2Column = DateColumn.create("date2",
						Arrays.asList(new LocalDate[] { date2 }));
				
				quote2Column = DoubleColumn.create(deduplicator.check(ticker + " date2"),
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
		
		DeduplicateValues deduplicator = new DeduplicateValues();
		
		for (AssetAllocPerfInf inf : assetAllocPerfInfList) {						
			String ticker = inf.getTicker();

			DoubleColumn allocColumn = DoubleColumn.create(deduplicator.check(ticker + " (aa,%)"),
					Arrays.asList(new Double[] { new Double(inf.getAllocationPercent()) }));
			
			assInfColumnList.add(allocColumn);
			
			double stockQuote1 = inf.getStockQuoteStart();

			DoubleColumn quote1Column = DoubleColumn.create(deduplicator.check(ticker + " date1"),
					Arrays.asList(new Double[] { new Double(stockQuote1) }));
			
			DoubleColumn quote2Column = null;
			
			if (inf.getAllocModelType() == AllocChoiceModelType.Momentum && inf instanceof MomAssetAllocPerfInf) {		
				double perfPercent = ((MomAssetAllocPerfInf)inf).getPercGrowth();
				
				double stockQuote2 = ((MomAssetAllocPerfInf)inf).getStockQuoteEnd();
				
				DoubleColumn tickerPerfColumn = DoubleColumn.create(deduplicator.check(ticker + " (perf,%)"),
						Arrays.asList(new Double[] { new Double(perfPercent) }));
				
				quote2Column = DoubleColumn.create(deduplicator.check(ticker + " date2"),
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
		
		DeduplicateValues deduplicator = new DeduplicateValues();
		
		for (AssetAllocPerfInf inf : assetAllocPerfInfList) {
			String ticker = inf.getTicker();

			if (inf.getAllocModelType() == AllocChoiceModelType.Momentum && inf instanceof MomAssetAllocPerfInf) {			
				double quoteValue = ((MomAssetAllocPerfInf)inf).getStockQuoteEnd();
				
				DoubleColumn quoteColumn = DoubleColumn.create(deduplicator.check(ticker),
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
		
		DeduplicateValues deduplicator = new DeduplicateValues();
		
		for (AssetAllocPerfInf inf : assetAllocPerfInfList) {
			String ticker = inf.getTicker();

			if (inf.getAllocModelType() == AllocChoiceModelType.Momentum && inf instanceof MomAssetAllocPerfInf) {			
				double perfPercent = ((MomAssetAllocPerfInf)inf).getPercGrowth();
				
				DoubleColumn perfColumn = DoubleColumn.create(deduplicator.check(ticker),
						Arrays.asList(new Double[] { new Double(perfPercent) }));
				
				perfColumnList.add(perfColumn);
			}			
		}
		
		return perfColumnList;
	}

	
	private List<Column<?>> fillTimingAssetsTable(List<AssetAllocPerfInf> assetAllocPerfInfList) {
		// asset alloc table
		List<Column<?>> asetsAllocColumnList = new ArrayList<>();

		DeduplicateValues deduplicator = new DeduplicateValues();
		
		for (AssetAllocPerfInf inf : assetAllocPerfInfList) {
			String ticker = inf.getTicker();

			double allocPersent = inf.getAllocationPercent();

			DoubleColumn allocColumn = DoubleColumn.create(deduplicator.check(ticker),
					Arrays.asList(new Double[] { new Double(allocPersent) }));

			asetsAllocColumnList.add(allocColumn);
		}

		return asetsAllocColumnList;
	}
	
	public void putRiskOnOffInfForMovingAverage(LocalDateTime date, boolean riskValue, 
			MovingAverageAssetAllocInf inf) {
		BooleanColumn riskResColumn = BooleanColumn.create("result",
				Arrays.asList(new Boolean[] { new Boolean(riskValue) }));

		DeduplicateValues deduplicator = new DeduplicateValues();
		
		DoubleColumn signalColumn = DoubleColumn.create(deduplicator.check(inf.getTicker()),
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

	public void putPortBalance(Map<LocalDateTime, Double> portBalance) {
		for(LocalDateTime date : portBalance.keySet() ) {
			DateTimeColumn dateColumn = DateTimeColumn.create("date", Arrays.asList(new LocalDateTime[] { date }));

			DoubleColumn balanceColumn = DoubleColumn.create("balance",
				Arrays.asList(new Double[] { portBalance.get(date)} ));
			
			monthlyRetunsTable = appendColumns("monthly returns", monthlyRetunsTable, dateColumn, balanceColumn);
		}
	}

	public void putStockQuantityInPort(TimingModel timingModel,
			PositionSet postionsOnDates) {
		List<TickerInf> tickersId = timingModel.getPortTickers();

		tickersId.addAll((Arrays.asList(
				new TickerInf[] {timingModel.getOutOfMarketPosTicker() } )));
		
		for (LocalDateTime curDate : postionsOnDates.getDates() ) {
			List<Column<?>> columnList = new ArrayList<>();
			
			DeduplicateValues deduplicator = new DeduplicateValues();
			
			for (TickerInf ticker : tickersId) {

				double quantityStock = 0;

				for (PositionInformation position : postionsOnDates.getPositions(curDate) ) {
					if (position.getTickerInf().equals(ticker))
						quantityStock = position.getQuantity();
				}
				
				DoubleColumn quantityColumn = DoubleColumn.create(
						deduplicator.check(ticker.getTicker()),
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
