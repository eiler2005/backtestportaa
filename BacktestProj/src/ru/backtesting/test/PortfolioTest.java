package ru.backtesting.test;

import java.util.Arrays;
import java.util.List;

import ru.backtesting.mktindicators.RSIOscillatorSignal;
import ru.backtesting.mktindicators.base.MarketIndicatorInterval;
import ru.backtesting.mktindicators.base.MarketIndicatorType;
import ru.backtesting.mktindicators.ma.MovingAverageIndicatorSignal;
import ru.backtesting.port.AssetAllocation;
import ru.backtesting.port.Portfolio;
import ru.backtesting.port.metrics.PortfolioMetrics;
import ru.backtesting.rebalancing.Frequency;
import ru.backtesting.rebalancing.RebalancingMethod;
import ru.backtesting.rebalancing.RebalancingType;

public class PortfolioTest {
	public void testBestPort() {
		Portfolio simplePort = new Portfolio("spy - 200", Arrays.asList(
				new AssetAllocation("SPY", 100)), 
				2004, 2018, 10000, 
				new RebalancingType(Frequency.Monthly, RebalancingMethod.AssetProportion), 
				Arrays.asList(new MovingAverageIndicatorSignal(50, 200, MarketIndicatorType.WMA_IND, MarketIndicatorInterval.Daily), 
						new RSIOscillatorSignal(100, MarketIndicatorInterval.Daily)), "TLT", false);
		
		simplePort = new Portfolio("spy - 200", Arrays.asList(
				new AssetAllocation("SPY", 100)), 
				2004, 2018, 10000, 
				new RebalancingType(Frequency.Monthly, RebalancingMethod.AssetProportion), 
				Arrays.asList(new MovingAverageIndicatorSignal(200, MarketIndicatorType.WMA_IND, MarketIndicatorInterval.Daily, 1), 
						new RSIOscillatorSignal(100, MarketIndicatorInterval.Daily)), "TLT", false);
	}
	
	public static void main(String[] args) {
		// !!!best
		/*Portfolio simplePort = new Portfolio("spy - 200", Arrays.asList(
				new AssetAllocation("SPY", 100)), 
				2004, 2018, 10000, 
				new RebalancingType(Frequency.Monthly, RebalancingMethod.AssetProportion), 
				Arrays.asList(new MovingAverageIndicatorSignal(50, 200, MovingAverageType.Weighted, 1), 
						new RSIOscillatorSignal(100)), "TLT", false);*/
		
		Portfolio simplePort = new Portfolio("spy - 200", Arrays.asList(
				new AssetAllocation("SPY", 100)), 
				2004, 2018, 10000, 
				new RebalancingType(Frequency.Monthly, RebalancingMethod.AssetProportion), 
				Arrays.asList(new MovingAverageIndicatorSignal(200, MarketIndicatorType.WMA_IND, MarketIndicatorInterval.Daily, 1),
						new RSIOscillatorSignal(100, MarketIndicatorInterval.Daily)), "TLT", false);
		
		List<AssetAllocation> spyAndTlt =  Arrays.asList(
				new AssetAllocation("SPY", 60), new AssetAllocation("TLT", 40));
		/*
		Portfolio simplePort = new Portfolio("spy/tlt - 60 on 40", spyAndTlt, 2004, 2018, 10000, 
				new RebalancingType(Frequency.Annually, RebalancingMethod.AssetProportion), 
				null, null, false);
		*/
		
		/*
		Portfolio simplePort = new Portfolio("spy/tlt - 60 on 40 with rsi", spyAndTlt, 2004, 2018, 10000, 
				new RebalancingType(Frequency.Monthly, RebalancingMethod.AssetProportion), 
				Arrays.asList(new RSIOscillatorSignal(100)), Portfolio.CASH_TICKER, false);
		*/

		/*
		Portfolio simplePort = new Portfolio("spy/tlt - 60 on 40 with sma200", spyAndTlt, 2017, 2018, 10000, 
				new RebalancingType(Frequency.Monthly, RebalancingMethod.AssetProportion), 
				Arrays.asList(new SMATechIndicatorSignal(50, 200)), 
				"BND", false); */
		
		/*
		Portfolio simplePort = new Portfolio("spy with cmo20", 
				Arrays.asList(new AssetAllocation("SPY", 100)), 2004, 2018, 10000, 
		new RebalancingType(Frequency.Monthly, RebalancingMethod.AssetProportion), 
		Arrays.asList(new ChandeMomentumOscillator(20)), "TLT", false);
		*/
		
		/*
		List<AssetAllocation> sectors = Arrays.asList(new AssetAllocation("XLV", 100),
				new AssetAllocation("XLP", 100), new AssetAllocation("XLU", 100), 
				new AssetAllocation("XLK", 100), 
				new AssetAllocation("XLF", 100), 
				new AssetAllocation("XLY", 100), new AssetAllocation("XLI", 100), 
				new AssetAllocation("XLE", 100), new AssetAllocation("XLB", 100));

		
		Portfolio simplePort = new Portfolio("sectors with cmo20", 
				sectors, 2004, 2018, 10000, 
		new RebalancingType(Frequency.Monthly, RebalancingMethod.AssetProportion), 
			Arrays.asList(new ChandeMomentumOscillator(50)), "TLT", false);
		 */
		
		simplePort.fillQuotesData();

		// simplePort.print(System.out);

		simplePort.backtestPortfolio();
		
		PortfolioMetrics metrics = new PortfolioMetrics(simplePort);
		//metrics.calcDrawdown();
		
		System.out.println(metrics.CAGRInPercent());

	}
}
