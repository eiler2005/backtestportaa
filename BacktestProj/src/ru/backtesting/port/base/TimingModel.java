package ru.backtesting.port.base;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

import ru.backtesting.mktindicators.base.MarketIndicatorInterface;
import ru.backtesting.port.base.aa.AssetAllocPerfInf;
import ru.backtesting.port.base.aa.AssetAllocationChoiceModel;
import ru.backtesting.port.base.ticker.Ticker;
import ru.backtesting.port.base.ticker.TickerInf;
import ru.backtesting.stockquotes.TradingTimeFrame;
import ru.backtesting.utils.Logger;
import ru.backtesting.utils.doubles.TickerDeduplicator;

public class TimingModel {
	private List<? extends AssetAllocation> assetsAllocFixed;

	private List<TickerInf> assetAllocTickers;
	private TickerInf outOfMarketPosTicker;
	
	private List<MarketIndicatorInterface> riskControlSignals;
	private TradingTimeFrame period;
	private AssetAllocationChoiceModel allocChoiceModel;

	private void checkTickers(List<? extends AssetAllocation> assetsAlloc, List<String> tickers) {
		if ( CollectionUtils.isNotEmpty(assetsAlloc) && TickerDeduplicator.instance().hasDuplicateAlloc(assetsAlloc) )
			throw new IllegalArgumentException("В аллокациях типа " + AssetAllocation.class.getSimpleName() + " не может быть одинаковых тикеров. Проверьте настройки - [" + assetsAlloc.toString() + "]!");
			
		if ( CollectionUtils.isNotEmpty(tickers) && TickerDeduplicator.instance().hasDuplicate(tickers) )
			throw new IllegalArgumentException("В аллокациях типа " + AssetAllocation.class.getSimpleName() + " не может быть одинаковых тикеров. Проверьте настройки - [" + tickers + "]!");
		
		List<String> allTickers = new ArrayList<>();
		
		if ( CollectionUtils.isNotEmpty(assetsAllocFixed) )
			for (AssetAllocation aa : assetsAllocFixed ) {
				String aaTicker = aa.getTicker();

				if ( tickers != null && tickers.contains(aaTicker) )
					aa.generateId();
				
				allTickers.add(aaTicker);
			}

		if ( CollectionUtils.isNotEmpty(tickers) ) {
			assetAllocTickers = new ArrayList<TickerInf>();
			
			for(String ticker : tickers) 
				assetAllocTickers.add(new Ticker(ticker));
				
			allTickers.addAll(tickers);
		}

		if ( allTickers.contains(outOfMarketPosTicker.getTicker()) )
			outOfMarketPosTicker.generateId();
							
		if (allTickers.size() == 0)
			throw new Error("Переменные assetsAllocFixed и assetAllocTickers не могут быть пустыми. Проверьте код на ошибки");
	}
	
	public List<AssetAllocPerfInf> calculateAllocationsBySignals(LocalDateTime date, LocalDateTime launchDate) {
		List<AssetAllocPerfInf> allocPerfInfList = allocChoiceModel.calculateAllocation(date, assetsAllocFixed,
				assetAllocTickers, outOfMarketPosTicker, launchDate);

		Logger.log().info("Держим следующие активы в портфеле: " + allocPerfInfList);

		return allocPerfInfList;
	}

	public TradingTimeFrame getTimeFrame() {
		return period;
	}

	public List<TickerInf> getPortTickers() {
		List<TickerInf> allTickers = new ArrayList<>();

		if (  CollectionUtils.isNotEmpty(assetsAllocFixed) )
			for (int i = 0; i < assetsAllocFixed.size(); i++) {
				TickerInf ticker = assetsAllocFixed.get(i);

				allTickers.add(ticker);
			}

		if ( CollectionUtils.isNotEmpty(assetAllocTickers) )
			allTickers.addAll(assetAllocTickers);

		allTickers.add(Ticker.cash());
		
		if (  allTickers.size() == 0)
			throw new Error(
					"Переменные assetsAllocFixed и assetAllocTickers не могут быть пустыми. Проверьте код на ошибки");

		return allTickers;
	}

	public TickerInf getOutOfMarketPosTicker() {
		return outOfMarketPosTicker;
	}
	
	public List<? extends AssetAllocation> getFixedAllocations() {
		return assetsAllocFixed;
	}

	public AssetAllocationChoiceModel getAllocChoiceModel() {
		return allocChoiceModel;
	}

	public List<MarketIndicatorInterface> getRiskControlSignals() {
		return riskControlSignals;
	}

	@Deprecated
	private boolean haveTimingSignals() {
		return CollectionUtils.isNotEmpty(riskControlSignals);
	}

	public Map<String, Object> getExportModelParams() {
		Map<String, Object> portParamsMap = new LinkedHashMap<String, Object>();

		portParamsMap.putAll(allocChoiceModel.getExportModelParams());

		portParamsMap.put("out of market ticker", outOfMarketPosTicker.getTicker());

		return portParamsMap;
	}

	@Deprecated
	public Map<String, Object> getRiskControlParams() {
		Map<String, Object> portParamsMap = new LinkedHashMap<String, Object>();

		// portParamsMap.putAll(allocChoiceModel.getExportModelParams());

		portParamsMap.put("risk control signals", "not done yet");

		return portParamsMap;
	}
	
	public static class Builder {
		private TimingModel model;
		
		private List<String> tickers;
		
		private Builder() {
			model = new TimingModel();
		}
		
		public Builder(List<? extends AssetAllocation> assetsAlloc, AssetAllocationChoiceModel allocChoiceModel,
			TradingTimeFrame timeFrame) {
			this();
			
			model.assetsAllocFixed = assetsAlloc;

			model.period = timeFrame;
			
			model.allocChoiceModel = allocChoiceModel;
		}
		
		public Builder(AssetAllocationChoiceModel allocChoiceModel, TradingTimeFrame timeFrame, List<String> tickers) {
			this();
			
			this.tickers = tickers;

			model.period = timeFrame;
				
			model.allocChoiceModel = allocChoiceModel;
		}
		
		public Builder(List<? extends AssetAllocation> assetsAlloc, List<String> tickers,
				AssetAllocationChoiceModel allocChoiceModel, TradingTimeFrame timeFrame) {
			this();
			
			this.tickers = tickers;

			model.assetsAllocFixed = assetsAlloc;
			
			model.period = timeFrame;
				
			model.allocChoiceModel = allocChoiceModel;
		}
		
		public Builder setRiskControlSignals(List<MarketIndicatorInterface> riskControlSignals) {
			model.riskControlSignals = riskControlSignals;
			
			return this;
		}
		
		public Builder setOutOfMarketPosTicker(String outOfMarketTicker) {
			model.outOfMarketPosTicker = new Ticker(outOfMarketTicker);
			
			return this;
		}
		
		public TimingModel build() {
			model.checkTickers(model.assetsAllocFixed, tickers);
			
			return model;
		}
	}
}
