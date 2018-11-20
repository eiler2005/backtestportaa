package ru.backtesting.types;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.patriques.AlphaVantageConnector;
import org.patriques.TimeSeries;
import org.patriques.output.timeseries.MonthlyAdjusted;
import org.patriques.output.timeseries.data.StockData;

import ru.backtesting.main.Main;
import ru.backtesting.stockquotes.StockConnector;
import ru.backtesting.stockquotes.StockQuote;
import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.types.rebalancing.RebalancingType;

public class Portfolio {
	private String name;
	int startYear;
	int endYear;
	final int initialAmount;
	final RebalancingType rebalancing;
	final List<AssetAllocation> assetsAllocation;
	LinkedHashMap<LocalDateTime, List<PositionInformation>> historyPositions;
	private double currPortfolioPrice;
    private final DecimalFormat df;
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
		historyPositions = new LinkedHashMap<LocalDateTime, List<PositionInformation>>();
		currPortfolioPrice = initialAmount;
		df = new DecimalFormat("0.00");
		this.reinvestDividends = reinvestDividends;
	}

	public void fillQuotesData() {		
		// портфель забивается данными - без расчета цены и т п
		for (AssetAllocation asset : assetsAllocation) {
			String ticker = asset.getTicker();
			
			List<LocalDateTime> dates = StockQuoteHistory.storage().fillQuotesData(ticker, startYear, endYear);

			for (LocalDateTime date : dates)
				if ( historyPositions.get(date) == null ) {
					List<PositionInformation> otherPositions = new ArrayList <PositionInformation> ();
					otherPositions.add(new PositionInformation(ticker, date));
					
					historyPositions.put(date, otherPositions);
				}
				else {
					List<PositionInformation> otherPositions = historyPositions.get(date);
					otherPositions.add(new PositionInformation(ticker, date));
				}
			}
	}
	
	public void backtestPortfolio(PrintStream stream) {
		currPortfolioPrice = 0;
		
		int count = 0;
			    
	    LocalDateTime prevDate = null;
	    
		for(LocalDateTime date: historyPositions.keySet() ) {
			stream.println("date: " + date);
			
			List<PositionInformation> positions = historyPositions.get(date);
			
			if (count == 0) { // покупка активов в самом начале
				
				currPortfolioPrice = buyPortfolio(positions, assetsAllocation, initialAmount, reinvestDividends);
								
				count++;				
			}
			else {
				List<PositionInformation> prevPositions = historyPositions.get(prevDate);
				
				// пересчитать портфель по новым ценам активов - продать активы
				
				double pricePortfolioOnDate = calculatePortfolioOnDate(positions, assetsAllocation, prevPositions);
				
				// купить активы на измененную сумму портфеля в соответствии с долями
				
				stream.println("Изменение портфеля составило (с ребалансировкой): " + df.format(pricePortfolioOnDate - currPortfolioPrice) + " по сравнению с предыдущим периодом");
				
				currPortfolioPrice = buyPortfolio(positions, assetsAllocation, pricePortfolioOnDate, reinvestDividends);
			}
			
			prevDate = date;
			
			stream.println("Стоимость портфеля на [" + date + "] : " + df.format(currPortfolioPrice));
			
			stream.println("-------------");
		}
	}
	
	private double buyPortfolio(List<PositionInformation> positions, List<AssetAllocation> assetsAllocation, double moneyAmount, boolean dividends) {
		double price = 0;
		
		for (PositionInformation position : positions) {
			String ticker = position.getTicker();
			double currentQuote = StockQuoteHistory.storage().getQuoteValueByDate(ticker, position.getTime(), dividends);
								
			double quantity = calculateQuntityStocks(ticker, currentQuote, moneyAmount, assetsAllocation);
			
			position.setQuantity(quantity);
			
			System.out.println("Купили [ticker: " + position.getTicker() + "] " + df.format(quantity) + 
					" лотов, на сумму " + df.format(currentQuote*quantity));

			price += currentQuote*quantity;
		}
		
		System.out.println("Купили активов на сумму: " + df.format(price));
		
		return price;
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
		
		System.out.println("Посчитали стоимость портфеля на новую дату (изменение с прошлого периода): " + df.format(price));
		
		return price;		
	}
	
	private double calculateQuntityStocks(String ticker, double currentPrice, 
			double portfolioPrice, List<AssetAllocation> assetsAllocation) {
		for (AssetAllocation stock : assetsAllocation)
			if (stock.getTicker().equalsIgnoreCase(ticker) )
				return ((stock.getAllocation()/100)*portfolioPrice/currentPrice);
		
		throw new RuntimeException("В портфеле нет актива с тикером:" + ticker);
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
		
		for(LocalDateTime date: historyPositions.keySet() ) {
			stream.println("date: " + date);
			
			List<PositionInformation> positions = historyPositions.get(date);
			
			for (PositionInformation position : positions) {
				StockQuote quote = StockQuoteHistory.storage().getQuoteForDate(position.getTicker(), position.getTime());
				
				System.out.println("____quantity:   " + position.getQuantity());
			    System.out.println("____price: " + position.getQuantity()*quote.getClose());
			    System.out.println("____open:   " + quote.getOpen());
			    System.out.println("____high:   " + quote.getHigh());
			    System.out.println("____low:    " + quote.getLow());
			    System.out.println("____close:  " + quote.getClose());
			    System.out.println("____adjClose:  " + quote.getAdjustedClose());
			}
			
			stream.println("-------------");
		}
	}
}
