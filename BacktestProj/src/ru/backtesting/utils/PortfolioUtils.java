package ru.backtesting.utils;

import java.util.ArrayList;
import java.util.List;

import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.types.AssetAllocation;
import ru.backtesting.types.PositionInformation;

public class PortfolioUtils {
	public static double buyPortfolio(List<PositionInformation> positions, List<AssetAllocation> assetsAllocation, double moneyAmount, boolean dividends) {
		double price = 0;
				
		for (PositionInformation position : positions) {
			String ticker = position.getTicker();
			double currentQuote = StockQuoteHistory.storage().getQuoteValueByDate(ticker, position.getTime(), dividends);
								
			double quantity = calculateQuntityStocks(ticker, currentQuote, moneyAmount, assetsAllocation);
			
			position.update(quantity, currentQuote*quantity);
			
			System.out.println("Купили [ticker: " + position.getTicker() + "] " +Logger.log().doubleLog(quantity) + 
					" лотов, на сумму " + Logger.log().doubleLog(currentQuote*quantity));

			price += currentQuote*quantity;
		}
		
		System.out.println("Купили активов на сумму: " + Logger.log().doubleLog(price));
		
		return price;
	}
	
	public static double calculateQuntityStocks(String ticker, double currentPrice, 
			double portfolioPrice, List<AssetAllocation> assetsAllocation) {
		for (AssetAllocation stock : assetsAllocation)
			if (stock.getTicker().equalsIgnoreCase(ticker) )
				return ((stock.getAllocation()/100)*portfolioPrice/currentPrice);
		
		throw new RuntimeException("В портфеле нет актива с тикером:" + ticker);
	}
}
