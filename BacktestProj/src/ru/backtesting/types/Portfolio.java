package ru.backtesting.types;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import ru.backtesting.signal.SignalActionContext;
import ru.backtesting.signal.SignalTestingAction;
import ru.backtesting.stockquotes.StockQuote;
import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.types.rebalancing.RebalancingMethod;
import ru.backtesting.types.rebalancing.RebalancingType;
import ru.backtesting.utils.Logger;
import ru.backtesting.utils.PortfolioUtils;

public class Portfolio {
	public final String CASH_TICKER = "CASH__TICKER";
	private String name;
	int startYear;
	int endYear;
	final int initialAmount;
	final RebalancingType rebalType;
	final List<AssetAllocation> assetsAllocation;
	private LinkedHashMap<LocalDateTime, List<PositionInformation>> postionsOnDates;
    private boolean reinvestDividends = false;
    private List<SignalActionContext> timingSignals;
	private HashMap<LocalDateTime, PositionInformation> outOfMarketPositions;
	private String outOfMarketPosTicker;
    
	public Portfolio(String name, List<AssetAllocation> assetsAllocation, int startYear, int endYear, int initialAmount,
			RebalancingType rebalancing, List<SignalActionContext> timingSignals, boolean reinvestDividends) {
		super();
		this.name = name;
		this.assetsAllocation = assetsAllocation;
		this.startYear = startYear;
		this.endYear = endYear;
		this.initialAmount = initialAmount;
		this.rebalType = rebalancing;
		this.postionsOnDates = new LinkedHashMap<LocalDateTime, List<PositionInformation>>();
		this.reinvestDividends = reinvestDividends;
		this.timingSignals = timingSignals;
	}
	
	public Portfolio(String name, List<AssetAllocation> assetsAllocation, int startYear, int endYear, int initialAmount,
			RebalancingType rebalancing, List<SignalActionContext> timingSignals, String outOfMarketTicker, boolean reinvestDividends) {
		super();
		this.name = name;
		this.assetsAllocation = assetsAllocation;
		this.startYear = startYear;
		this.endYear = endYear;
		this.initialAmount = initialAmount;
		this.rebalType = rebalancing;
		this.postionsOnDates = new LinkedHashMap<LocalDateTime, List<PositionInformation>>();
		this.reinvestDividends = reinvestDividends;
		
		this.outOfMarketPositions = new LinkedHashMap<LocalDateTime, PositionInformation>();
		this.outOfMarketPosTicker = outOfMarketTicker;
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
	
	public void backtestPortfolio() {
	    // BuyAndHold: купил в начале срока - продал в конце
	    if (rebalType.getRebalMethod().equals(RebalancingMethod.BuyAndHold)) {
	    	// посчитать -----
			Logger.log().info("Портфель типа Buy&Hold: дата покупки - ");
			
			// купить по первой дате
			
			// currPortfolioPrice = PortfolioUtils.buyPortfolio(postionsOnDates.get(key), assetsAllocation, initialAmount, reinvestDividends);
			
			// продать по последней
			
			// разница - профит
			
			// ------------- ДОДЕЛАТЬ
	    }
	    else {
			Logger.log().info("Портфель типа TimingPortfolio с ребалансировкой активов");
			
		    // 1. отфильтровать портфель по частоте ребаланса

			
			// размечаем что и когда покупаем и продаем - без реальной покупки и продажи
	    	for(LocalDateTime date: postionsOnDates.keySet() ) {
				Logger.log().info("Рассматриваем сигналы по портфелю на дату: " + date);
				
				List<PositionInformation> positions = postionsOnDates.get(date);
				
				// можно ли находиться в позиции, которая вне рынка по сигналам или только outOfMarketPos
				if (outOfMarketPositions != null && haveTimingSignals()) {
					PositionInformation outOfMarketPos = new PositionInformation(outOfMarketPosTicker, date);
					if (!isBuyForSignals(date, outOfMarketPosTicker, timingSignals))
						outOfMarketPos.sell();
					outOfMarketPositions.put(date, outOfMarketPos);
				}
				
				// проверяем можно ли держать позицию в портфеле по сигналам
				for (PositionInformation position : positions)
					if ( haveTimingSignals() && !isBuyForSignals(date, position.getTicker(), timingSignals) )
						// продаем позицию и передаем деньги под другие позиции или вне рынка
						position.sell();
	    	}
	    	
	    	// теперь уже проводим покупку и продажу
	    	// тут не забываем про - вне рынка
	    	
	    	LocalDateTime prevDate = null;
	    	double prevPortfolioBalance = initialAmount;
	    	int iterator = 0;
	    	
	    	for (LocalDateTime date: postionsOnDates.keySet() ) {
				Logger.log().info("Формирование портфеля на дату: " + date);

				List<PositionInformation> positions = postionsOnDates.get(date);
					
				if (iterator != 0)
					prevPortfolioBalance = PortfolioUtils.calculateAllPositionsBalance(postionsOnDates.get(prevDate), date, reinvestDividends);
				
	    		for (int i = 0; i < assetsAllocation.size(); i++) {
		    		String ticker = assetsAllocation.get(i).getTicker();
		    		
		    		PositionInformation position = positions.get(i);
		    				    		
		    		if ( position.isHoldInPortfolio() ) {
		    			// купить в соответствии с assetAllocation
		    			
		    			double quote = StockQuoteHistory.storage().getQuoteValueByDate(ticker, position.getTime(), reinvestDividends);
		    			
		    			double quantity = PortfolioUtils.calculateQuantityStocks(ticker, quote, prevPortfolioBalance, assetsAllocation);
		    					
						Logger.log().info("Купили в портфель [" + ticker + "] " + Logger.log().doubleLog(quantity) + " лотов на сумму " + Logger.log().doubleLog(quantity*quote) + 
								", цена лота: " + Logger.log().doubleLog(quote) );
		    			
		    			position.buy(quantity, quantity*quote);
		    		}
		    		else { // купить в соответствии с outOfMarketPos и других аллокаций
		    			// перекладываем текущую позицию в outOfMarketPos
		    			// positions.set(i, outOfMarketPos)
		    		}
		    	}
	    		
				double portfolioBalance = PortfolioUtils.calculateAllPositionsBalance(positions); 
	    		
				Logger.log().info("Стоимость портфеля на [" + date + "] : " + Logger.log().doubleLog(portfolioBalance));
				
				Logger.log().info("-------------");
	    		
	    		prevDate = date;
	    		prevPortfolioBalance = portfolioBalance;
	    		iterator++;
	    	}
	    	
	    	// ----
	    	
	    	/*
	    	
	    	if (count == 0) { // покупка активов в самом начале
	    		currPortfolioPrice = PortfolioUtils.buyPortfolio(positions, assetsAllocation, initialAmount, reinvestDividends);
									
				count++;				
			}
			else {
				List<PositionInformation> prevPositions = postionsOnDates.get(prevDate);
					
				// пересчитать портфель по новым ценам активов - продать активы
					
				double pricePortfolioOnDate = calculatePortfolioOnDate(positions, assetsAllocation, prevPositions);
					
				// купить активы на измененную сумму портфеля в соответствии с долями
					
				Logger.log().info("Изменение портфеля составило (с ребалансировкой): " + Logger.log().doubleLog(pricePortfolioOnDate - currPortfolioPrice) + " по сравнению с предыдущим периодом");
					
				currPortfolioPrice = PortfolioUtils.buyPortfolio(positions, assetsAllocation, pricePortfolioOnDate, reinvestDividends);
			}
				
			prevDate = date;
				
			Logger.log().info("Стоимость портфеля на [" + date + "] : " + Logger.log().doubleLog(currPortfolioPrice));
				
			Logger.log().info("-------------");
			
			*/
		}
	}
	
	private boolean haveTimingSignals() {
		return timingSignals != null && timingSignals.size() != 0;
	}
	
	private boolean isBuyForSignals(LocalDateTime date, String ticker, List<SignalActionContext> timingSignals) {		
		if (ticker.equals(CASH_TICKER))
			return true;
			
		if (timingSignals != null && timingSignals.size() != 0)
			for (SignalActionContext signal : timingSignals) {
				if (signal.testSignal(date, ticker) == -1 )
					return false;
			}
		else
			throw new RuntimeException("Для тикера \"" +  ticker + "\" не найдены технические индикаторы"); 
		
		return true;
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
		return rebalType;
	}

	public List<AssetAllocation> getAssetsAllocation() {
		return assetsAllocation;
	}

	public void print() {
		Logger.log().info("Portfolio: " + name);
		Logger.log().info("=============");
		
		for (AssetAllocation asset : assetsAllocation) {
			String ticker = asset.getTicker();
			Logger.log().info("Ticker: " + ticker + ", allocation - " + asset.getAllocationPercent() + " %");
		}
		
		for(LocalDateTime date: postionsOnDates.keySet() ) {
			Logger.log().info("date: " + date);
			
			List<PositionInformation> positions = postionsOnDates.get(date);
			
			for (PositionInformation position : positions) {
				StockQuote quote = StockQuoteHistory.storage().getQuoteByDate(position.getTicker(), position.getTime());
				
				Logger.log().info("____quantity:   " + position.getQuantity());
				Logger.log().info("____price: " + position.getQuantity()*quote.getClose());
				Logger.log().info("____open:   " + quote.getOpen());
				Logger.log().info("____high:   " + quote.getHigh());
				Logger.log().info("____low:    " + quote.getLow());
				Logger.log().info("____close:  " + quote.getClose());
				Logger.log().info("____adjClose:  " + quote.getAdjustedClose());
			}
			
			Logger.log().info("-------------");
		}
	}
}
