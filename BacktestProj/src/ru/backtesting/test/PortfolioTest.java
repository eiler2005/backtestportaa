package ru.backtesting.test;

import java.util.Arrays;
import java.util.List;

import ru.backtesting.mktindicators.ChandeMomentumOscillator;
import ru.backtesting.mktindicators.RSIOscillatorSignal;
import ru.backtesting.mktindicators.base.MarketIndicatorType;
import ru.backtesting.mktindicators.ma.MovingAverageIndicatorSignal;
import ru.backtesting.port.Portfolio;
import ru.backtesting.port.base.AllocChoiceModelType;
import ru.backtesting.port.base.AssetAllocationBase;
import ru.backtesting.port.base.TimingModel;
import ru.backtesting.port.base.aa.AssetAllocationChoiceModel;
import ru.backtesting.port.base.aa.FixedAssetAllocModel;
import ru.backtesting.port.base.aa.momentum.DualMomentumAllocModel;
import ru.backtesting.port.base.aa.sma.DynAssetAllocWithSMA;
import ru.backtesting.port.metrics.PortfolioMetrics;
import ru.backtesting.port.results.BacktestResultsStorage;
import ru.backtesting.rebalancing.Frequency;
import ru.backtesting.rebalancing.TimingModelInf;
import ru.backtesting.rebalancing.TimingModelType;
import ru.backtesting.stockquotes.TradingTimeFrame;
import ru.backtesting.utils.Logger;

public class PortfolioTest {
	public static Portfolio testBestPort() {
		return new Portfolio("spy - 200",  
				2004, 2020, 10000, 
				new TimingModel(Arrays.asList(new AssetAllocationBase("SPY", 100)), new FixedAssetAllocModel(), TradingTimeFrame.Daily, 
						Arrays.asList(new MovingAverageIndicatorSignal(200, MarketIndicatorType.WMA_IND, TradingTimeFrame.Daily, 1), 
								new RSIOscillatorSignal(100, TradingTimeFrame.Daily)), "TLT"),
				new TimingModelInf(Frequency.Monthly, TimingModelType.AssetAllocationTiming), 
				false);
	}
	
	@Deprecated
	public static Portfolio sectorsCMOIndTest() {		
		List<AssetAllocationBase> sectors = Arrays.asList(new AssetAllocationBase("XLV", 100),
				new AssetAllocationBase("XLP", 100), new AssetAllocationBase("XLU", 100), 
				new AssetAllocationBase("XLK", 100), 
				new AssetAllocationBase("XLF", 100), 
				new AssetAllocationBase("XLY", 100), new AssetAllocationBase("XLI", 100), 
				new AssetAllocationBase("XLE", 100), new AssetAllocationBase("XLB", 100));

		return new Portfolio("spy - 200",  
				2004, 2020, 10000, 
				new TimingModel(sectors, new FixedAssetAllocModel(), TradingTimeFrame.Daily, 
						Arrays.asList(new ChandeMomentumOscillator(50, TradingTimeFrame.Daily)), "TLT"),
				new TimingModelInf(Frequency.Monthly, TimingModelType.AssetAllocationTiming), 
				false);
	}
	
	// PVZ Link - https://bit.ly/2XGnvTI
	public static Portfolio port6040test() {
		List<AssetAllocationBase> spyAndTltAlloc =  Arrays.asList(
				new AssetAllocationBase("SPY", 60), new AssetAllocationBase("TLT", 40));

		TimingModel model = new TimingModel(spyAndTltAlloc, new FixedAssetAllocModel(), TradingTimeFrame.Daily, null);
		
		return new Portfolio("spy/tlt - 60 on 40", 2010, 2019, 10000, model , 
				new TimingModelInf(Frequency.Monthly, TimingModelType.AssetAllocationTiming), false);
	}
	
	public static Portfolio sectorsPortWide() {
		List<String> tickers = Arrays.asList(
				"Iai", "Igv", "Fdn", "Skyy", "Xitk", "Ihi", "Skyy",
				"Igv", "Xitk", "Ita", "Gdx", "Sil", "Ura", "Tan", "Pick", "Kre",
				"emqq", "hack", 
				"arkw", "arkk", "finx", "hero", "ibb" ,"ibuy", "ogig", "qqq", "smh", "xbi");

		// bug котировки с 2019-10-31, xitk - котировки с 2016, clou - 2019-04-16
		
		MovingAverageIndicatorSignal sma = new MovingAverageIndicatorSignal(160, MarketIndicatorType.SMA_IND, TradingTimeFrame.Daily, 0);
		
		AssetAllocationChoiceModel aaModelChoice = new DynAssetAllocWithSMA("SPY", sma, 2, 8);
		
		TimingModel model = new TimingModel(aaModelChoice, TradingTimeFrame.Daily, null, "ief", tickers);
		
		return new Portfolio("dynamic asset alloc with sma", 2017, 2019, 10000, model, 
				new TimingModelInf(Frequency.Monthly, TimingModelType.AssetAllocationTiming), false);
	}
	
	public static Portfolio iyoskaPortNew() {
		List<String> tickers = Arrays.asList("LQD", "HYG", "QQQ", "SPY", "EFA", "EEM", "SCZ", "DIA");

		MovingAverageIndicatorSignal sma = new MovingAverageIndicatorSignal(160, MarketIndicatorType.SMA_IND, TradingTimeFrame.Daily, 0);
		
		AssetAllocationChoiceModel aaModelChoice = new DynAssetAllocWithSMA("SPY", sma, 1, 4);
		
		TimingModel model = new TimingModel(aaModelChoice, TradingTimeFrame.Daily, null, "ief", tickers);
		
		return new Portfolio("dynamic asset alloc with sma", 2008, 2019, 10000, model, 
				new TimingModelInf(Frequency.Monthly, TimingModelType.AssetAllocationTiming), false);
	}
	
	// PVZ Link - https://bit.ly/37NXpSs
	public static Portfolio iyoskaPort() {
		List<String> tickers = Arrays.asList("ihi", "smh", "QQQ", "xbi", "ita");

		MovingAverageIndicatorSignal sma = new MovingAverageIndicatorSignal(160, MarketIndicatorType.SMA_IND, TradingTimeFrame.Daily, 0);
		
		AssetAllocationChoiceModel aaModelChoice = new DynAssetAllocWithSMA("SPY", sma);
		
		TimingModel model = new TimingModel(aaModelChoice, TradingTimeFrame.Daily, null, "TLT", tickers);
		
		return new Portfolio("dynamic asset alloc with sma", "https://bit.ly/37NXpSs", 2007, 2019, 10000, model, 
				new TimingModelInf(Frequency.Monthly, TimingModelType.AssetAllocationTiming), false);
	}
	
	// PVZ Link - https://bit.ly/2MouZEM
	public static Portfolio dualMomPortTest() {		
		List<String> tickers = Arrays.asList("LQD", "HYG", "QQQ", "SPY", "EFA", "EEM");

		AssetAllocationChoiceModel aaModelChoice = new DualMomentumAllocModel("SPY", "SHY", 2, 4);
		
		TimingModel model = new TimingModel(aaModelChoice, TradingTimeFrame.Daily, null, "IEF", tickers);
		
		return new Portfolio("dual mom port test", "https://bit.ly/2MouZEM", 2008, 2019, 10000, model, 
				new TimingModelInf(Frequency.Quarterly, TimingModelType.AssetAllocationTiming), false);
	}
	
	/*
	 * seeking alpha post - https://bit.ly/3dFlSLN
	 * PVZ Link - https://bit.ly/2YHkIcr
	 */
	public static Portfolio tomaDualMomPortTest() {		
		List<String> tickers = Arrays.asList("MDY", "QQQ");

		AssetAllocationChoiceModel aaModelChoice = new DualMomentumAllocModel("VBMFX", "SHY", 2, 1);
		
		TimingModel model = new TimingModel(aaModelChoice, TradingTimeFrame.Daily, null, "IEF", tickers);
		
		return new Portfolio("dual mom port test", "https://bit.ly/2YHkIcr", 2004, 2019, 10000.00, model, 
				new TimingModelInf(Frequency.Quarterly, TimingModelType.AssetAllocationTiming), false);
	}
	
	public static void main(String[] args) {
		/* !!!best
		Portfolio simplePort = new Portfolio("spy tltl gld", Arrays.asList(
				new AssetAllocation("SPY", 60), new AssetAllocation("TLT", 20), new AssetAllocation("gld", 20)), 
				2006, 2020, 10000, 
				new RebalancingType(Frequency.Monthly, RebalancingMethod.AssetProportion), TradingTimeFrame.Monthly,
				Arrays.asList(new MovingAverageIndicatorSignal(50, 200, MarketIndicatorType.WMA_IND, TradingTimeFrame.Daily, 1),
						new RSIOscillatorSignal(100, TradingTimeFrame.Daily)), "TLT", false);
		*/
		
		
		/* Portfolio simplePort = new Portfolio("spy - 200", Arrays.asList(
				new AssetAllocation("SPY", 60), new AssetAllocation("TLT", 40) ), 
				2004, 2019, 10000, 
				new RebalancingType(Frequency.Monthly, RebalancingMethod.AssetProportion), TradingTimeFrame.Monthly,
				Arrays.asList(new MovingAverageIndicatorSignal(200, MarketIndicatorType.WMA_IND, TradingTimeFrame.Daily, 1),
						new RSIOscillatorSignal(100, TradingTimeFrame.Daily)), "TLT", false);
		*/
		
		 /* Portfolio simplePort = new Portfolio("qqq - kama", Arrays.asList(
				new AssetAllocation("QQQ", 100)), 
				2004, 2019, 10000, 
				new RebalancingType(Frequency.Daily, RebalancingMethod.AssetProportion), TradingTimeFrame.Daily,
				Arrays.asList(new MovingAverageIndicatorSignal(20, 60, MarketIndicatorType.KaufmanAdaptiveMA_IND, TradingTimeFrame.Daily, 0.5)), 
				Portfolio.CASH_TICKER, false);
		*/

		// Portfolio simplePort = port6040test();
		
		// Portfolio simplePort = tomaDualMomPortTest();
		
		Portfolio simplePort = iyoskaPort();
		
		// Portfolio simplePort = sectorsPortWide();
		
		// Portfolio simplePort = testBestPort();
		
		// Portfolio simplePort = sectorsCMOIndTest();
		
		/*
		Portfolio simplePort = new Portfolio("spy/tlt - 60 on 40 with sma200", spyAndTlt, 2017, 2018, 10000, 
				new RebalancingType(Frequency.Monthly, RebalancingMethod.AssetProportion), 
				Arrays.asList(new SMATechIndicatorSignal(50, 200)), 
				"BND", false); */
		

		simplePort.fillQuotesData();

		Logger.log().info("Print port:" + simplePort);

		simplePort.backtestPortfolio();

		PortfolioMetrics metrics = new PortfolioMetrics(simplePort);
		metrics.calcDrawdown();

		Logger.log().info("cagr (percent) = " + metrics.CAGRInPercent());

		simplePort.putPortMetrics(0, metrics.CAGRInPercent());

		simplePort.writeBackTestResultsToExcel();
	}
}
