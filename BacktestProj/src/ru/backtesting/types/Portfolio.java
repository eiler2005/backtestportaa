package ru.backtesting.types;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import ru.backtesting.stockquotes.StockQuote;
import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.types.rebalancing.RebalancingType;
import ru.backtesting.utils.Logger;
import ru.backtesting.utils.PortfolioUtils;

public class Portfolio {
	private String name;
	int startYear;
	int endYear;
	final int initialAmount;
	final RebalancingType rebalancing;
	final List<AssetAllocation> assetsAllocation;
	LinkedHashMap<LocalDateTime, List<PositionInformation>> postionsOnDates;
	private double currPortfolioPrice;
    private boolean reinvestDividends = false;
	
	public Portfolio(String name, List<AssetAllocation> assetsAllocation, int startYear, int endYear, int initialAmount,
			RebalancingType rebalancing, boolean reinvestDividends) {
		super();
		this.name = name;
		this.assetsAllocation = assetsAllocation;
		this.startYear = startYear;
		this.endYear = endYear;
		this.initialAmount = initialAmount;
		this.rebalancing = rebalancing;
		postionsOnDates = new LinkedHashMap<LocalDateTime, List<PositionInformation>>();
		currPortfolioPrice = initialAmount;
		this.reinvestDividends = reinvestDividends;
	}

	public void fillQuotesData() {		
		// портфель забивается данными - без расчета цены и т п
		for (AssetAllocation asset : assetsAllocation) {
			String ticker = asset.getTicker();
			
			List<LocalDateTime> dates = StockQuoteHistory.storage().fillQuotesData(ticker, startYear, endYear);

			for (LocalDateTime date : dates)
				if ( postionsOnDates.get(date) == null ) {
					List<PositionInformation> otherPositions = new ArrayList <PositionInformation> ();
					otherPositions.add(new PositionInformation(ticker, date));
					
					postionsOnDates.put(date, otherPositions);
				}
				else {
					List<PositionInformation> otherPositions = postionsOnDates.get(date);
					otherPositions.add(new PositionInformation(ticker, date));
				}
			}
	}
	
	public void backtestPortfolio(PrintStream stream) {
		currPortfolioPrice = 0;
		
		int count = 0;
			    
	    LocalDateTime prevDate = null;
	    
		for(LocalDateTime date: postionsOnDates.keySet() ) {
			stream.println("date: " + date);
			
			List<PositionInformation> positions = postionsOnDates.get(date);
			
			if (count == 0) { // покупка активов в самом начале
				
				currPortfolioPrice = PortfolioUtils.buyPortfolio(positions, assetsAllocation, initialAmount, reinvestDividends);
								
				count++;				
			}
			else {
				List<PositionInformation> prevPositions = postionsOnDates.get(prevDate);
				
				// пересчитать портфель по новым ценам активов - продать активы
				
				double pricePortfolioOnDate = calculatePortfolioOnDate(positions, assetsAllocation, prevPositions);
				
				// купить активы на измененную сумму портфеля в соответствии с долями
				
				stream.println("Изменение портфеля составило (с ребалансировкой): " + Logger.log().doubleLog(pricePortfolioOnDate - currPortfolioPrice) + " по сравнению с предыдущим периодом");
				
				currPortfolioPrice = PortfolioUtils.buyPortfolio(positions, assetsAllocation, pricePortfolioOnDate, reinvestDividends);
			}
			
			prevDate = date;
			
			stream.println("Стоимость портфеля на [" + date + "] : " + Logger.log().doubleLog(currPortfolioPrice));
			
			stream.println("-------------");
		}
	}
	
	private double calculatePortfolioOnDate(List<PositionInformation> newPricePositions, List<AssetAllocation> assetsAllocation, List<PositionInformation> quantityPosit) {
		double price = 0;
		
		for (PositionInformation quantP : quantityPosit) {
			String ticker = quantP.getTicker();
			
			for (PositionInformation position : newPricePositions)
				if (position.getTicker().equals(ticker)) {
					double currentQuote = StockQuoteHistory.storage().getQuoteValueByDate(ticker, position.getTime(), reinvestDividends);
					
					// надо понять сколько у нас есть денег - продажа актива по новым ценам
					
					price += currentQuote*quantP.getQuantity();
				}
				else {
					// throw new RuntimeException("В портфеле нет актива с тикером:" + ticker);
				}
		}
		
		System.out.println("Посчитали стоимость портфеля на новую дату (изменение с прошлого периода): " + Logger.log().doubleLog(price));
		
		return price;		
	}
	
	public int getStartYear() {
		return startYear;
	}

	public int getEndYear() {
		return endYear;
	}

	public int getInitialAmount() {
		return initialAmount;
	}


	public RebalancingType getRebalancing() {
		return rebalancing;
	}

	public List<AssetAllocation> getAssetsAllocation() {
		return assetsAllocation;
	} 
	
	public void print(PrintStream stream) {
		stream.println("Portfolio: " + name);
		stream.println("=============");
		
		for (AssetAllocation asset : assetsAllocation) {
			String ticker = asset.getTicker();
			stream.println("Ticker: " + ticker + ", allocation - " + asset.getAllocation() + " %");
		}
		
		for(LocalDateTime date: postionsOnDates.keySet() ) {
			stream.println("date: " + date);
			
			List<PositionInformation> positions = postionsOnDates.get(date);
			
			for (PositionInformation position : positions) {
				StockQuote quote = StockQuoteHistory.storage().getQuoteForDate(position.getTicker(), position.getTime());
				
				stream.println("____quantity:   " + position.getQuantity());
				stream.println("____price: " + position.getQuantity()*quote.getClose());
				stream.println("____open:   " + quote.getOpen());
				stream.println("____high:   " + quote.getHigh());
				stream.println("____low:    " + quote.getLow());
				stream.println("____close:  " + quote.getClose());
				stream.println("____adjClose:  " + quote.getAdjustedClose());
			}
			
			stream.println("-------------");
		}
	}
}
