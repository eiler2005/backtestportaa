package ru.backtesting.test;

import java.util.Arrays;
import java.util.List;

import ru.backtesting.mktindicators.ChandeMomentumOscillator;
import ru.backtesting.mktindicators.RSIOscillatorSignal;
import ru.backtesting.mktindicators.base.MarketIndicatorType;
import ru.backtesting.mktindicators.ma.MovingAverageIndicatorSignal;
import ru.backtesting.port.CoreSatellitePortfolio;
import ru.backtesting.port.Portfolio;
import ru.backtesting.port.base.AssetAllocation;
import ru.backtesting.port.base.AssetAllocationBase;
import ru.backtesting.port.base.TimingModel;
import ru.backtesting.port.base.aa.AssetAllocationChoiceModel;
import ru.backtesting.port.base.aa.FixedAssetAllocModel;
import ru.backtesting.port.base.aa.momentum.DualMomentumAllocModel;
import ru.backtesting.port.base.aa.sma.DynAssetAllocWithSMA;
import ru.backtesting.port.metrics.PortfolioMetrics;
import ru.backtesting.port.signals.FixedLossOrProfitSignalHandler;
import ru.backtesting.port.signals.PositionSignalHandler;
import ru.backtesting.port.signals.TopOverMovingAverFixedHandler;
import ru.backtesting.rebalancing.Frequency;
import ru.backtesting.rebalancing.TimingModelInf;
import ru.backtesting.rebalancing.TimingModelType;
import ru.backtesting.stockquotes.TradingTimeFrame;
import ru.backtesting.utils.Logger;

public class PortfolioTest {
	// close, without dividends = false
	private static final boolean STOCK_PRICE_VALUE_DEFAULT = false;

	// adj close, with dividends = true
	private static final boolean STOCK_PRICE_VALUE_ADJUSTED = true;
	
	public static Portfolio testBestPort() {
		TimingModel model = new TimingModel
						.Builder(Arrays.asList(new AssetAllocationBase("SPY", 100)),
								new FixedAssetAllocModel(), TradingTimeFrame.Daily)
						.setRiskControlSignals(Arrays.asList(
								new MovingAverageIndicatorSignal(200, MarketIndicatorType.WMA_IND,
										TradingTimeFrame.Daily, 1),
								new RSIOscillatorSignal(100, TradingTimeFrame.Daily)))
						.setOutOfMarketPosTicker("TLT")
						.build();

		return new Portfolio("spy - 200", 2004, 2020, 10000, model,
				new TimingModelInf(Frequency.Monthly, TimingModelType.AssetAllocationTiming), STOCK_PRICE_VALUE_DEFAULT);
	}

	@Deprecated
	public static Portfolio sectorsCMOIndTest() {
		List<? extends AssetAllocation> sectors = Arrays.asList(new AssetAllocationBase("XLV", 100),
				new AssetAllocationBase("XLP", 100), new AssetAllocationBase("XLU", 100),
				new AssetAllocationBase("XLK", 100), new AssetAllocationBase("XLF", 100),
				new AssetAllocationBase("XLY", 100), new AssetAllocationBase("XLI", 100),
				new AssetAllocationBase("XLE", 100), new AssetAllocationBase("XLB", 100));

		TimingModel model = new TimingModel.Builder(sectors, new FixedAssetAllocModel(), TradingTimeFrame.Daily)
				.setRiskControlSignals(Arrays.asList(
						new MovingAverageIndicatorSignal(200, MarketIndicatorType.WMA_IND, TradingTimeFrame.Daily, 1),
						new ChandeMomentumOscillator(50, TradingTimeFrame.Daily)))
				.setOutOfMarketPosTicker("TLT")
				.build();

		return new Portfolio("spy - 200", 2004, 2020, 10000, model,
				new TimingModelInf(Frequency.Monthly, TimingModelType.AssetAllocationTiming), STOCK_PRICE_VALUE_DEFAULT);
	}

	// PVZ Link - https://bit.ly/2XGnvTI
	public static Portfolio port6040test() {
		List<? extends AssetAllocation> spyAndTltAlloc = Arrays.asList(new AssetAllocationBase("SPY", 60),
				new AssetAllocationBase("TLT", 40));

		TimingModel model = new TimingModel.Builder(spyAndTltAlloc, new FixedAssetAllocModel(), TradingTimeFrame.Daily)
				.build();

		return new Portfolio("spy/tlt - 60 on 40", 2010, 2019, 10000, model,
				new TimingModelInf(Frequency.Monthly, TimingModelType.AssetAllocationTiming), STOCK_PRICE_VALUE_DEFAULT);
	}

	public static Portfolio sectorsPortWide() {
		List<String> tickers = Arrays.asList("Iai", "Igv", "Fdn", "Skyy", "Ihi", "Xitk", "Ita", "Gdx", "Sil", "Ura",
				"Tan", "Pick", "Kre", "emqq", "hack", "bug", "clou", "hero", "ogig", "arkw", "arkk", "finx", "ibb",
				"ibuy", "qqq", "smh", "xbi");

		MovingAverageIndicatorSignal sma = new MovingAverageIndicatorSignal(160, MarketIndicatorType.SMA_IND,
				TradingTimeFrame.Daily, 0);

		AssetAllocationChoiceModel aaModelChoice = new DynAssetAllocWithSMA("SPY", sma, 1, 8);

		TimingModel model = new TimingModel.Builder(aaModelChoice, TradingTimeFrame.Daily, tickers)
				.setOutOfMarketPosTicker("TLT")
				.build();

		return new Portfolio("dynamic asset alloc with sma", 2006, 2020, 10000, model,
				new TimingModelInf(Frequency.Monthly, TimingModelType.AssetAllocationTiming), STOCK_PRICE_VALUE_DEFAULT);
	}

	public static Portfolio iyoskaPortNew() {
		List<String> tickers = Arrays.asList("LQD", "HYG", "QQQ", "SPY", "EFA", "EEM", "SCZ", "DIA");

		MovingAverageIndicatorSignal sma = new MovingAverageIndicatorSignal(160, MarketIndicatorType.SMA_IND,
				TradingTimeFrame.Daily, 0);

		AssetAllocationChoiceModel aaModelChoice = new DynAssetAllocWithSMA("SPY", sma, 1, 4);

		TimingModel model = new TimingModel.Builder(aaModelChoice, TradingTimeFrame.Daily, tickers)
				.setOutOfMarketPosTicker("ief")
				.build();

		return new Portfolio("dynamic asset alloc with sma", 2008, 2019, 10000, model,
				new TimingModelInf(Frequency.Monthly, TimingModelType.AssetAllocationTiming), STOCK_PRICE_VALUE_DEFAULT);
	}

	// PVZ Link - https://bit.ly/37NXpSs
	public static Portfolio iyoskaPortWithFixedAA() {
		List<? extends AssetAllocation> fixedAlloc = Arrays.asList(new AssetAllocationBase("qqq", 28),
				new AssetAllocationBase("xbi", 18), new AssetAllocationBase("IHI", 18),
				new AssetAllocationBase("ita", 18), new AssetAllocationBase("SMH", 18));

		MovingAverageIndicatorSignal sma = new MovingAverageIndicatorSignal(160, MarketIndicatorType.SMA_IND,
				TradingTimeFrame.Daily, 0);

		AssetAllocationChoiceModel aaModelChoice = new DynAssetAllocWithSMA("SPY", sma);

		TimingModel model = new TimingModel.Builder(fixedAlloc, aaModelChoice, TradingTimeFrame.Daily)
				.setOutOfMarketPosTicker("tlt").build();

		PositionSignalHandler fixPosHandler = new FixedLossOrProfitSignalHandler(-65);
		
		return new Portfolio("dynamic asset alloc with sma", "https://bit.ly/37NXpSs", 2007, 2020, 10000, model,
				new TimingModelInf(Frequency.Monthly, TimingModelType.AssetAllocationTiming), fixPosHandler, STOCK_PRICE_VALUE_DEFAULT);
	}

	// PVZ Link - https://bit.ly/37NXpSs
	public static Portfolio iyoskaPort() {
		List<String> tickers = Arrays.asList("ihi", "smh", "QQQ", "xbi", "ita"/* , "ogig", "emqq" */);

		MovingAverageIndicatorSignal sma = new MovingAverageIndicatorSignal(160, MarketIndicatorType.SMA_IND,
				TradingTimeFrame.Daily, 0);

		AssetAllocationChoiceModel aaModelChoice = new DynAssetAllocWithSMA("SPY", sma);

		TimingModel model = new TimingModel.Builder(aaModelChoice, TradingTimeFrame.Daily, tickers)
				.setOutOfMarketPosTicker("tlt")
				.build();

		PositionSignalHandler fixPosHandler = new FixedLossOrProfitSignalHandler(115);
		
		return new Portfolio("dynamic asset alloc with sma", "https://bit.ly/37NXpSs", 2007, 2020, 10000, model,
				new TimingModelInf(Frequency.Monthly, TimingModelType.AssetAllocationTiming), fixPosHandler, STOCK_PRICE_VALUE_DEFAULT);
	}

	// PVZ Link - https://bit.ly/2ZeBPCE
		public static Portfolio iyoskaPortWithNewCore() {
			List<? extends AssetAllocation> coreAlloc = Arrays.asList(
					new AssetAllocationBase("spy", 30),
					new AssetAllocationBase("tlt", 50),
					new AssetAllocationBase("vxx", 20));
			
			// при qld ставить цены adjusted или dividends = true
			List<? extends AssetAllocation> satAlloc = Arrays.asList(new AssetAllocationBase("qld", 100));
			
			
			//PositionSignalHandler exitSignal = new TopOverMovingAverFixedHandler("spy", 10);
			
			// PositionSignalHandler exitSignal = new FixedLossOrProfitSignalHandler(115);
			
			MovingAverageIndicatorSignal sma = new MovingAverageIndicatorSignal(160, MarketIndicatorType.SMA_IND,
					TradingTimeFrame.Daily, 0);

			AssetAllocationChoiceModel aaModelChoice = new DynAssetAllocWithSMA("spy", sma);

			CoreSatellitePortfolio portfolio = new CoreSatellitePortfolio.Builder("core sat port with sma", 1995, 2020, 10000, 
						new TimingModelInf(Frequency.Monthly, TimingModelType.AssetAllocationTiming), STOCK_PRICE_VALUE_ADJUSTED)
					.setPvzLink("https://bit.ly/2ZeBPCE")
					.addAllocationWeights(40, 60)
					.setOutOfMarketPosTicker("tlt")
					.setAllocationsFixed(coreAlloc, satAlloc, aaModelChoice, TradingTimeFrame.Daily)
				//	.addPosSignalHandler(exitSignal)
					.build();
			
			return portfolio;
		}
	
	// PVZ Link - https://bit.ly/2ZeBPCE
	public static Portfolio iyoskaPortWithCore() {
		List<? extends AssetAllocation> coreAlloc = Arrays.asList(
				new AssetAllocationBase("ief", 80),
				new AssetAllocationBase("gld", 20));
		
		//List<? extends AssetAllocation> satAlloc = Arrays.asList(new AssetAllocationBase("qqq", 28),
		//		new AssetAllocationBase("xbi", 18), new AssetAllocationBase("IHI", 18),
		//		new AssetAllocationBase("ita", 18), new AssetAllocationBase("SMH", 18));
		
		// при qld ставить цены adjusted или dividends = true
		List<? extends AssetAllocation> satAlloc = Arrays.asList(new AssetAllocationBase("qld", 100));
		
		// List<String> satTickers = Arrays.asList("ihi", "smh", "QQQ", "xbi", "ita");

		
		PositionSignalHandler exitSignal = new TopOverMovingAverFixedHandler("spy", 110);
		
		// PositionSignalHandler exitSignal = new FixedLossOrProfitSignalHandler(115);
		
		MovingAverageIndicatorSignal sma = new MovingAverageIndicatorSignal(160, MarketIndicatorType.SMA_IND,
				TradingTimeFrame.Daily, 0);

		AssetAllocationChoiceModel aaModelChoice = new DynAssetAllocWithSMA("spy", sma);

		CoreSatellitePortfolio portfolio = new CoreSatellitePortfolio.Builder("core sat port with sma", 2007, 2020, 10000, 
					new TimingModelInf(Frequency.Monthly, TimingModelType.AssetAllocationTiming), true)
				.setPvzLink("https://bit.ly/2ZeBPCE")
				.addAllocationWeights(40, 60)
				.setOutOfMarketPosTicker("tlt")
				.setAllocationsFixed(coreAlloc, satAlloc, aaModelChoice, TradingTimeFrame.Daily)
				//.setAllocationsSoft(coreAlloc, satTickers, aaModelChoice, TradingTimeFrame.Daily)
				.addPosSignalHandler(exitSignal)
				.build();
		
		return portfolio;
	}

	// PVZ Link - https://bit.ly/2MouZEM
	public static Portfolio dualMomPortTest() {
		List<String> tickers = Arrays.asList("LQD", "HYG", "QQQ", "SPY", "EFA", "EEM");

		AssetAllocationChoiceModel aaModelChoice = new DualMomentumAllocModel("VBMFX", "SHY", 2, 4);

		TimingModel model = new TimingModel.Builder(aaModelChoice, TradingTimeFrame.Daily, tickers)
				.setOutOfMarketPosTicker("ief")
				.build();
		
		return new Portfolio("dual mom port test", "https://bit.ly/2MouZEM", 2008, 2020, 10000, model,
				new TimingModelInf(Frequency.Quarterly, TimingModelType.AssetAllocationTiming), null, STOCK_PRICE_VALUE_DEFAULT);
	}

	/*
	 * seeking alpha post - https://bit.ly/3dFlSLN 
	 * PVZ Link - https://bit.ly/2YHkIcr
	 */
	public static Portfolio tomaDualMomPortTest() {
		List<String> tickers = Arrays.asList("MDY", "QQQ");
		
		// AssetAllocationChoiceModel aaModelChoice = new DualMomentumAllocModel("VBMFX", "SHY", 2, 1);
		
		AssetAllocationChoiceModel aaModelChoice = new DualMomentumAllocModel("VBMFX", "SHY", 2, 1);
		
		TimingModel model = new TimingModel.Builder(aaModelChoice, TradingTimeFrame.Daily, tickers)
				.setOutOfMarketPosTicker("ief")
				.build();
		
		return new Portfolio("dual mom port test", "https://bit.ly/2YHkIcr", 2005, 2019, 10000.00, model,
				new TimingModelInf(Frequency.Monthly, TimingModelType.AssetAllocationTiming), null, STOCK_PRICE_VALUE_DEFAULT);
	}
	
	/*
	 *  seeking alpha post - https://bit.ly/2Zkagrz
	 *  PVZ Link - https://bit.ly/3eM3j9h
	 *  Vanguard Communication Services Index Fund (NYSEARCA: VOX)
		Vanguard Consumer Discretionary Index Fund (NYSEARCA: VCR)
		Vanguard Consumer Staples Index Fund (NYSEARCA: VDC)
		Vanguard Energy Index Fund (NYSEARCA: VDE)
		Vanguard Financials Index Fund (NYSEARCA: VFH)
		Vanguard Health Care Index Fund (NYSEARCA: VHT)
		Vanguard Industrials Index Fund (NYSEARCA: VIS)
		Vanguard Information Technology Index Fund (NYSEARCA: VGT)
		Vanguard Materials Index Fund (NYSEARCA: VAW)
		Vanguard Real Estate Index Fund (NYSEARCA: VNQ)
		Vanguard Utilities Index Fund (NYSEARCA: VPU)
		iShares 7-10 Year Treasury Bond ETF (NYSEARCA: IEF)
		Vanguard Total Bond Market Index (MUTF: VBMFX)
	 */
	public static Portfolio tomaSectorsMomPort() {
		List<String> tickers = Arrays.asList("VOX", "VCR", "VDC", "VDE", "VFH", "VHT",
				"VIS", "VGT", "VAW", "VNQ", "VPU");

		AssetAllocationChoiceModel aaModelChoice = new DualMomentumAllocModel("VBMFX", "SHY", 2, 4);

		TimingModel model = new TimingModel.Builder(aaModelChoice, TradingTimeFrame.Daily, tickers)
				.setOutOfMarketPosTicker("ief")
				.build();
		
		return new Portfolio("dual mom port test", "https://bit.ly/3eM3j9h", 2007, 2020, 10000.00, model,
				new TimingModelInf(Frequency.Quarterly, TimingModelType.AssetAllocationTiming), null, STOCK_PRICE_VALUE_DEFAULT);
	}

	public static void main(String[] args) {
		/*
		 * !!!best Portfolio simplePort = new Portfolio("spy tltl gld", Arrays.asList(
		 * new AssetAllocation("SPY", 60), new AssetAllocation("TLT", 20), new
		 * AssetAllocation("gld", 20)), 2006, 2020, 10000, new
		 * RebalancingType(Frequency.Monthly, RebalancingMethod.AssetProportion),
		 * TradingTimeFrame.Monthly, Arrays.asList(new MovingAverageIndicatorSignal(50,
		 * 200, MarketIndicatorType.WMA_IND, TradingTimeFrame.Daily, 1), new
		 * RSIOscillatorSignal(100, TradingTimeFrame.Daily)), "TLT", false);
		 */

		/*
		 * Portfolio simplePort = new Portfolio("spy - 200", Arrays.asList( new
		 * AssetAllocation("SPY", 60), new AssetAllocation("TLT", 40) ), 2004, 2019,
		 * 10000, new RebalancingType(Frequency.Monthly,
		 * RebalancingMethod.AssetProportion), TradingTimeFrame.Monthly,
		 * Arrays.asList(new MovingAverageIndicatorSignal(200,
		 * MarketIndicatorType.WMA_IND, TradingTimeFrame.Daily, 1), new
		 * RSIOscillatorSignal(100, TradingTimeFrame.Daily)), "TLT", false);
		 */

		/*
		 * Portfolio simplePort = new Portfolio("qqq - kama", Arrays.asList( new
		 * AssetAllocation("QQQ", 100)), 2004, 2019, 10000, new
		 * RebalancingType(Frequency.Daily, RebalancingMethod.AssetProportion),
		 * TradingTimeFrame.Daily, Arrays.asList(new MovingAverageIndicatorSignal(20,
		 * 60, MarketIndicatorType.KaufmanAdaptiveMA_IND, TradingTimeFrame.Daily, 0.5)),
		 * Portfolio.CASH_TICKER, false);
		 */

		// Portfolio simplePort = port6040test();

		// Portfolio simplePort = tomaDualMomPortTest();

		// Portfolio simplePort = iyoskaPort();

		// Portfolio simplePort = tomaSectorsMomPort();
		
		// Portfolio simplePort = iyoskaPortWithNewCore();

		Portfolio simplePort = iyoskaPortWithFixedAA();

		// Portfolio simplePort = sectorsPortWide();

		// Portfolio simplePort = testBestPort();

		// Portfolio simplePort = sectorsCMOIndTest();

		/*
		 * Portfolio simplePort = new Portfolio("spy/tlt - 60 on 40 with sma200",
		 * spyAndTlt, 2017, 2018, 10000, new RebalancingType(Frequency.Monthly,
		 * RebalancingMethod.AssetProportion), Arrays.asList(new
		 * SMATechIndicatorSignal(50, 200)), "BND", false);
		 */

		simplePort.fillQuotesData();

		simplePort.backtestPortfolio();

		PortfolioMetrics metrics = new PortfolioMetrics(simplePort);
		metrics.calcDrawdown();

		Logger.log().info("cagr (percent) = " + metrics.CAGRInPercent());
		Logger.log().info("max dd = " + metrics.getMaxDrawdown());

		simplePort.putPortMetrics(metrics.CAGRInPercent(), metrics.getMaxDrawdown(),
				metrics.getUnderwaterPeriodLenght());

		simplePort.writeBackTestResultsToExcel();
		
		Logger.log().info("-- Тест завершен --");

	}
}
