package ru.backtesting.utils;

import java.time.LocalDateTime;
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
								
			double quantity = calculateQuantityStocks(ticker, currentQuote, moneyAmount, assetsAllocation);
			
			position.buy(quantity, currentQuote*quantity);
			
			Logger.log().info("Купили [ticker: " + position.getTicker() + "] " +Logger.log().doubleLog(quantity) + 
					" лотов, на сумму " + Logger.log().doubleLog(currentQuote*quantity));

			price += currentQuote*quantity;
		}
		
		Logger.log().info("Купили активов на сумму: " + Logger.log().doubleLog(price));
		
		return price;
	}
	
	public static double calculateQuantityStocks(String ticker, double tickerQuote, 
			double portfolioBalance, List<AssetAllocation> assetsAllocation) {
		for (AssetAllocation stock : assetsAllocation)
			if (stock.getTicker().equalsIgnoreCase(ticker) )
				return ((stock.getAllocationPercent()/100)*portfolioBalance/tickerQuote);
		
		throw new RuntimeException("В портфеле нет актива с тикером:" + ticker);
	}
	
	public static double calculateAllPositionsBalance(List<PositionInformation> positions) {
		double sum = 0;
		
		for(PositionInformation pos : positions)
			sum += pos.getPrice();
		
		return sum;
	}
	
	public static double calculateAllPositionsBalance(List<PositionInformation> positions, LocalDateTime date, boolean reinvestDividends) {
		double sum = 0;
		
		for(PositionInformation pos : positions) {
			double quoteValue = StockQuoteHistory.storage().getQuoteValueByDate(pos.getTicker(), date, reinvestDividends);
			
			sum += pos.getQuantity()*quoteValue;
		}
			
		return sum;
	}
}
