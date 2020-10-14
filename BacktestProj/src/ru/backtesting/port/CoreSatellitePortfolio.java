package ru.backtesting.port;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import ru.backtesting.mktindicators.base.MarketIndicatorInterface;
import ru.backtesting.port.base.AllocationType;
import ru.backtesting.port.base.AssetAllocation;
import ru.backtesting.port.base.AssetAllocationBase;
import ru.backtesting.port.base.TimingModel;
import ru.backtesting.port.base.aa.AssetAllocationChoiceModel;
import ru.backtesting.port.base.aa.AssetAllocationUtils;
import ru.backtesting.port.signals.PositionSignalHandler;
import ru.backtesting.rebalancing.TimingModelInf;
import ru.backtesting.stockquotes.TradingTimeFrame;

public class CoreSatellitePortfolio extends Portfolio {
	private double coreAlloc = 0, satelliteAlloc = 0;
	
	private CoreSatellitePortfolio() {
		super();
	}
	
	public static class Builder {
		private List<AssetAllocation> assetsAllocFixed;
		private List<String> assetAllocSoftTickers;
		
		private String outOfMarketPosTicker;
		
		private List<MarketIndicatorInterface> riskControlSignals;
		
		private CoreSatellitePortfolio portfolio;

		private AssetAllocationChoiceModel aaModelChoice;

		private TradingTimeFrame timeFrame;
		
		private Builder() {
			portfolio = new CoreSatellitePortfolio();
			
			this.assetsAllocFixed = new ArrayList<AssetAllocation>();
			
			this.assetAllocSoftTickers = new ArrayList<String>();
		}
		
		public Builder(String name, int startYear, int endYear, int initialAmount,
				TimingModelInf timingModelInf, boolean reinvestDividends) {
			this();
			
			portfolio.name = name;
			portfolio.startYear = startYear;
			portfolio.endYear = endYear;
			portfolio.initialAmount = initialAmount;
			portfolio.timingModelInf = timingModelInf;
			
			portfolio.reinvestDividends = reinvestDividends;

		}
				
		public Builder addAllocationWeights(double core, double satellite) {
			portfolio.coreAlloc = core;
			portfolio.satelliteAlloc = satellite;
			
			if ( (core + satellite) != 100 )
				throw new IllegalArgumentException("Веса аллокаций для портфелей core и satellite установлены некорректно. Их сумма должна быть равна 100%");
				
			return this;
		}
		
		private void chechAllocOn100Percent(List<? extends AssetAllocation> assetsAlloc) {
			if ( AssetAllocationUtils.calcSummarizedAllocPercent(assetsAlloc) != 100 )
				throw new IllegalArgumentException("Веса аллокаций для портфелей core и satellite установлены некорректно. Их сумма должна быть равна 100%");
		}
		
		public Builder setAllocationsFixed(List<? extends AssetAllocation> coreAlloc, List<? extends AssetAllocation> satAlloc, AssetAllocationChoiceModel aaModelChoice, TradingTimeFrame timeFrame) {
			this.aaModelChoice = aaModelChoice;
			
			this.timeFrame = timeFrame;
						
			chechAllocOn100Percent(coreAlloc);
			chechAllocOn100Percent(satAlloc);
			
			if ( portfolio.coreAlloc == 0 || portfolio.satelliteAlloc == 0 )
				throw new IllegalArgumentException("Не установлены веса аллокаций для портфелей core и satellite. Проверьте внимательно код");
			
			for (AssetAllocation alloc : coreAlloc ) {
				alloc = new AssetAllocationBase(alloc.getTicker(), (double) alloc.getAllocationPercent()*portfolio.coreAlloc / 100, AllocationType.Core);
				this.assetsAllocFixed.add(alloc);
			}
					
			for (AssetAllocation alloc : satAlloc ) {
				alloc = new AssetAllocationBase(alloc.getTicker(), (double) alloc.getAllocationPercent()* portfolio.satelliteAlloc / 100, AllocationType.Satellite);
				this.assetsAllocFixed.add(alloc);
			}
			
			return this;
		}
		
		public Builder setAllocationsSoft(List<? extends AssetAllocation> coreAlloc, List<String> satAllocTickers, AssetAllocationChoiceModel aaModelChoice, TradingTimeFrame timeFrame) {
			this.aaModelChoice = aaModelChoice;

			this.timeFrame = timeFrame;
						
			chechAllocOn100Percent(coreAlloc);
			
			if ( portfolio.coreAlloc == 0 || portfolio.satelliteAlloc == 0 )
				throw new IllegalArgumentException("Не установлены веса аллокаций для портфелей core и satellite. Проверьте внимательно код");
			
			for (AssetAllocation alloc : coreAlloc ) {
				alloc = new AssetAllocationBase(alloc.getTicker(), (double) alloc.getAllocationPercent()*portfolio.coreAlloc / 100, AllocationType.Core);
				this.assetsAllocFixed.add(alloc);
			}
				
			this.assetAllocSoftTickers.addAll(satAllocTickers);
			
			return this;
		}
		
		public Builder setPvzLink(String pvzLink) {
			portfolio.pvzLink = pvzLink;
			
			return this;
		}
		
		public Builder setOutOfMarketPosTicker(String outOfMarketTicker) {
			this.outOfMarketPosTicker = outOfMarketTicker;
			
			return this;
		}
		
		public Builder addPosSignalHandler(PositionSignalHandler posSignalHandler) {
			portfolio.exitSignalHandler = posSignalHandler;
			
			return this;
		}
		
		public CoreSatellitePortfolio build() {
			if ( CollectionUtils.isEmpty(assetsAllocFixed) )
				throw new IllegalArgumentException("Не установлены аллокации для портфелей core и satellite. Проверьте внимательно код");
			
			if ( portfolio.coreAlloc == 0 || portfolio.satelliteAlloc == 0 )
				throw new IllegalArgumentException("Не установлены веса аллокаций для портфелей core и satellite. Проверьте внимательно код");

			TimingModel model = null;
			
			if ( CollectionUtils.isNotEmpty(assetsAllocFixed) && CollectionUtils.isNotEmpty(assetAllocSoftTickers) )
				model = new TimingModel.Builder(assetsAllocFixed, assetAllocSoftTickers, aaModelChoice, timeFrame)
					.setOutOfMarketPosTicker(outOfMarketPosTicker)
					.setRiskControlSignals(riskControlSignals)
					.build();
			else if ( CollectionUtils.isNotEmpty(assetsAllocFixed) ) {
				model = new TimingModel.Builder(assetsAllocFixed, aaModelChoice, timeFrame)
					.setOutOfMarketPosTicker(outOfMarketPosTicker)
					.setRiskControlSignals(riskControlSignals)
					.build();
			}
			
			portfolio.PortfolioConsctructor(portfolio.name, portfolio.pvzLink, portfolio.startYear, 
					portfolio.endYear, portfolio.initialAmount, model, portfolio.timingModelInf, portfolio.reinvestDividends);
			
			return portfolio;
		}
	}

	public double getCoreAlloc() {
		return coreAlloc;
	}

	public double getSatelliteAlloc() {
		return satelliteAlloc;
	}
}
