package ru.backtesting.types.test;

import java.util.Arrays;
import java.util.List;

import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.types.AssetAllocation;
import ru.backtesting.types.Portfolio;
import ru.backtesting.types.rebalancing.Frequency;
import ru.backtesting.types.rebalancing.RebalancingMethod;
import ru.backtesting.types.rebalancing.RebalancingType;

public class PortfolioTest {
	public static void main(String[] args) {
		List<AssetAllocation> spyAndTlt =  Arrays.asList(
				new AssetAllocation("SPY", 60), new AssetAllocation("TLT", 40));
		
		Portfolio simplePort = new Portfolio("spy/tlt - 60 on 40", spyAndTlt, 2007, 2016, 10000, 
				new RebalancingType(Frequency.Annually, RebalancingMethod.AssetProportion), null, false);
						
		simplePort.fillQuotesData();

		// simplePort.print(System.out);

		simplePort.backtestPortfolio();
		
	}
}
