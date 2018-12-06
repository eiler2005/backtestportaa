package ru.backtesting.test;

import java.util.Arrays;
import java.util.List;

import ru.backtesting.port.AssetAllocation;
import ru.backtesting.port.Portfolio;
import ru.backtesting.port.PositionInformation;
import ru.backtesting.port.metrics.PortfolioMetrics;
import ru.backtesting.rebalancing.Frequency;
import ru.backtesting.rebalancing.RebalancingMethod;
import ru.backtesting.rebalancing.RebalancingType;
import ru.backtesting.signal.BollingerBandsIndicatorSignal;
import ru.backtesting.signal.ChandeMomentumOscillator;
import ru.backtesting.signal.RSIOscillatorSignal;
import ru.backtesting.signal.SignalTestingAction;
import ru.backtesting.signal.ma.MovingAverageIndicatorSignal;
import ru.backtesting.signal.ma.MovingAverageType;

public class PortfolioTest {
	public static void main(String[] args) {
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
		
		
		// !!!best
		Portfolio simplePort = new Portfolio("spy - 200", Arrays.asList(
				new AssetAllocation("SPY", 100)), 
				2004, 2018, 10000, 
				new RebalancingType(Frequency.Monthly, RebalancingMethod.AssetProportion), 
				Arrays.asList(new MovingAverageIndicatorSignal(50, 200, MovingAverageType.Weighted), 
						new RSIOscillatorSignal(100), 
						new BollingerBandsIndicatorSignal(200)), "TLT", false);
		
		/*
		Portfolio simplePort = new Portfolio("spy/tlt - 60 on 40 with sma200", spyAndTlt, 2047, 2018, 10000, 
				new RebalancingType(Frequency.Monthly, RebalancingMethod.AssetProportion), 
				Arrays.asList(new SMATechIndicatorSignal(50, 200)), 
				"BND", false); */
		
		/*
		Portfolio simplePort = new Portfolio("spy with sma200", 
				Arrays.asList(new AssetAllocation("SPY", 100)), 2004, 2018, 10000, 
		new RebalancingType(Frequency.Monthly, RebalancingMethod.AssetProportion), 
		Arrays.asList(new SMATechIndicatorSignal(200)), "BND", false);*/
		/*
		Portfolio simplePort = new Portfolio("spy with cmo14", 
				Arrays.asList(new AssetAllocation("SPY", 100)), 2004, 2018, 10000, 
		new RebalancingType(Frequency.Monthly, RebalancingMethod.AssetProportion), 
			Arrays.asList(new ChandeMomentumOscillator(14)), "TLT", false);
		 */

		simplePort.fillQuotesData();

		// simplePort.print(System.out);

		simplePort.backtestPortfolio();
		
		PortfolioMetrics metrics = new PortfolioMetrics(simplePort);
		//metrics.calcDrawdown();
		
	}
}
