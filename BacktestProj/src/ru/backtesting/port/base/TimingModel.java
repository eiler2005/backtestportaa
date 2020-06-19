package ru.backtesting.port.base;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.backtesting.mktindicators.base.MarketIndicatorInterface;
import ru.backtesting.port.base.aa.AssetAllocPerfInf;
import ru.backtesting.port.base.aa.AssetAllocationChoiceModel;
import ru.backtesting.port.results.BacktestResultsStorage;
import ru.backtesting.stockquotes.TradingTimeFrame;
import ru.backtesting.utils.Logger;

public class TimingModel {
	private final List<AssetAllocationBase> assetsAlloc;
    private List<MarketIndicatorInterface> riskControlSignals;
	private String outOfMarketPosTicker;
    private TradingTimeFrame period;
    private AssetAllocationChoiceModel allocChoiceModel;
    
    
    public TimingModel(List<AssetAllocationBase> assetsAlloc, AssetAllocationChoiceModel allocChoiceModel, TradingTimeFrame timeFrame, 
    		List<MarketIndicatorInterface> riskControlSignals, String outOfMarketTicker) {
		super();
		
		this.assetsAlloc = assetsAlloc;

		this.riskControlSignals = riskControlSignals;
		this.period = timeFrame;
		
		this.outOfMarketPosTicker = outOfMarketTicker;
		
		this.allocChoiceModel = allocChoiceModel;
	}
    
    public TimingModel(List<AssetAllocationBase> assetsAlloc, AssetAllocationChoiceModel allocChoiceModel, TradingTimeFrame timeFrame, 
    		List<MarketIndicatorInterface> riskControlSignals) {
		super();
		
		this.assetsAlloc = assetsAlloc;

		this.riskControlSignals = riskControlSignals;
		this.period = timeFrame;
				
		this.allocChoiceModel = allocChoiceModel;
	}
    
    public TimingModel(AssetAllocationChoiceModel allocChoiceModel, TradingTimeFrame timeFrame, 
    		List<MarketIndicatorInterface> riskControlSignals, String outOfMarketTicker, List<String> tickers) {
    	if ( allocChoiceModel.getType() != AllocChoiceModelType.Momentum && 
    			allocChoiceModel.getType() != AllocChoiceModelType.MovingAveragesForAsset ) 
    		throw new IllegalArgumentException("В конструкторе модели с типом выбора активов " + allocChoiceModel.getType() + 
    				" нельзя указать тикеры списком[" + tickers  + "], нужно указать конкретные аллокации:");
    		
    	this.assetsAlloc = new ArrayList<>();
		
		for(String ticker : tickers)
			assetsAlloc.add(new AssetAllocationBase(ticker, 0));
		
		this.riskControlSignals = riskControlSignals;
		this.period = timeFrame;
		
		this.outOfMarketPosTicker = outOfMarketTicker;
		
		this.allocChoiceModel = allocChoiceModel;	
	}
    
    private List<AssetAllocPerfInf> filterAssetAllocPerfInfByParams(List<String> tickers, List<AssetAllocPerfInf> allocPerfInfList) {
    	List<AssetAllocPerfInf> list = new ArrayList<AssetAllocPerfInf>();
    	
    	Set<String> doublesTickers = new HashSet<String>();
    	
    	for(String ticker : tickers)
    		for(AssetAllocPerfInf inf : allocPerfInfList)
    			if (inf.getTicker().equalsIgnoreCase(ticker)) {
    				
    				// исключаем дубли - например abs mom perf growth spy и spy в аллокации активов портфеля
    				if (  !doublesTickers.contains(inf.getTicker().toLowerCase()) )
    					list.add(inf);
    				
    				doublesTickers.add(inf.getTicker().toLowerCase());
    			}
    	
    	return list;
    }
    
    public List<AssetAllocationBase> calculateAllocationsBySignals(LocalDateTime date, LocalDateTime launchDate ) {
    	List<AssetAllocPerfInf> allocPerfInfList = allocChoiceModel.calculateAllocation(date, getPortTickers(), outOfMarketPosTicker, launchDate);
    	
    	// put data in excel storage
    	putBackTestResultDataInStorage(date, allocPerfInfList, launchDate);
    	
    	// asset alloc for portfolio calculation
    	List<AssetAllocationBase> assetAlloc = new ArrayList<AssetAllocationBase>();
    	
    	for(AssetAllocPerfInf allocInf : allocPerfInfList) {
    		if ( allocInf.isHoldInPort() )
    			assetAlloc.add(new AssetAllocationBase(allocInf.getTicker(), allocInf.getAllocationPercent()));
    	}
    	
    	return assetAlloc;
    }
    
    public void putBackTestResultDataInStorage(LocalDateTime date, List<AssetAllocPerfInf> allocList, LocalDateTime launchDate) {
    	// detailed risk-onoff inf to excel
    	List<String> riskOnOffTickers = allocChoiceModel.getRiskOnOffTickers();
    	
    	List<AssetAllocPerfInf> riskOnOffAlloc = filterAssetAllocPerfInfByParams(riskOnOffTickers, allocList);
    	
    	Logger.log().info("Помещаем в хранилище результатов Excel данные о аллокациях: (riskOnOff) " + riskOnOffAlloc + " на дату " + date);
    	
    	BacktestResultsStorage.getInstance().putDetailedRiskOnOffInformation(launchDate, date, 
    			riskOnOffAlloc);
    	
    	// detailed other inf to excel
    	List<String> allTickers = getPortTickers();

    	// detailed other inf to excel
    	String outOfMarketPosTicker = getOutOfMarketPosTicker();
    	
    	allTickers.add(outOfMarketPosTicker);
    	
    	List<AssetAllocPerfInf> allTickersAlloc = filterAssetAllocPerfInfByParams(allTickers, allocList);
    	
    	Logger.log().info("Помещаем в хранилище результатов Excel данные о аллокациях: (allTickers + outOfMarket) " + allTickersAlloc + " на дату " + date);
    	
    	BacktestResultsStorage.getInstance().putDetailedBacktestInformation(launchDate, allTickersAlloc);
    }
    
	public TradingTimeFrame getTimeFrame() {
		return period;
	}
	
	public List<String> getPortTickers() {
		List<String> tickers = new ArrayList<String>();
		
		for (int i = 0; i < assetsAlloc.size(); i++) {
    		String ticker = assetsAlloc.get(i).getTicker();
    		
    		tickers.add(ticker);
		}
		
		//if ( outOfMarketPosTicker != null && !outOfMarketPosTicker.equals(Portfolio.CASH_TICKER) )
		//	tickers.add(outOfMarketPosTicker);
		
		return tickers;
	}
	

	public String getOutOfMarketPosTicker() {
		return outOfMarketPosTicker;
	}

	public List<AssetAllocationBase> getFixedAllocations() {
		return assetsAlloc;
	}
	
	public AssetAllocationChoiceModel getAllocChoiceModel() {
		return allocChoiceModel;
	}

	public List<MarketIndicatorInterface> getRiskControlSignals() {
		return riskControlSignals;
	}

	@Deprecated
	private boolean haveTimingSignals() {
		return riskControlSignals != null && riskControlSignals.size() != 0;
	}
	
	public Map<String, Object> getExportModelParams() {
		Map<String, Object> portParamsMap = new LinkedHashMap<String, Object>();
		
		portParamsMap.putAll(allocChoiceModel.getExportModelParams());
		
		portParamsMap.put("out of market ticker", outOfMarketPosTicker);
		
		return portParamsMap;
	}
	
	@Deprecated
	public Map<String, Object> getRiskControlParams() {
		Map<String, Object> portParamsMap = new LinkedHashMap<String, Object>();
		
		//portParamsMap.putAll(allocChoiceModel.getExportModelParams());
		
		portParamsMap.put("risk control signals", "not done yet");
		
		return portParamsMap;
	}
}
