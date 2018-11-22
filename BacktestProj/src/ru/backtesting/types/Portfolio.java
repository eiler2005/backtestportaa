package ru.backtesting.types;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.patriques.TimeSeries;
import org.patriques.output.timeseries.TimeSeriesResponse;
import org.patriques.output.timeseries.data.StockData;

import ru.backtesting.signal.SignalActionContext;
import ru.backtesting.stockquotes.StockConnector;
import ru.backtesting.stockquotes.StockQuote;
import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.types.rebalancing.Frequency;
import ru.backtesting.types.rebalancing.RebalancingMethod;
import ru.backtesting.types.rebalancing.RebalancingType;
import ru.backtesting.utils.Logger;
import ru.backtesting.utils.PortfolioUtils;

public class Portfolio {
	public final String CASH_TICKER = "CASH__TICKER";
	private String name;
	private int startYear;
	private int endYear;
	private final int initialAmount;
	private final RebalancingType rebalType;
	private final List<AssetAllocation> assetsAllocation;
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
			
			StockQuoteHistory.storage().fillQuotesData(ticker, startYear, endYear);

			List<LocalDateTime> dates = getTradingDates(ticker, startYear, endYear, rebalType.getFrequency());

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
	
	public List<LocalDateTime> getTradingDates(String ticker, int startYear, int endYear, Frequency period) {
		TimeSeriesResponse response = null;
		
		if (period.equals(Frequency.Weekly))
			response = new TimeSeries(StockConnector.fullConn()).weekly(ticker);
		else
			response = new TimeSeries(StockConnector.conn()).monthly(ticker);

	    List<StockData> stockData = response.getStockData();
		
	    Collections.reverse(stockData);
	    	    	
	    List<LocalDateTime> dates = new ArrayList<LocalDateTime>();
	    	    
	    dates.add(StockQuoteHistory.storage().getFirstTradedDay(ticker, startYear));
	    
	    for (int i = 0; i < stockData.size(); i++) {
			LocalDateTime date = stockData.get(i).getDateTime();
			
			if (date.getYear() >= startYear && date.getYear() <= endYear ) {
				Month month = date.getMonth();
				
				if ( i == 0 || (i == dates.size() - 1) )
					dates.add(date);
				else
				switch(period) {
					case Annually:				
			        	if (month.equals(Month.DECEMBER ) )
			        		dates.add(date);
						break;
					case SemiAnnually:
						if ( month.equals(Month.JUNE) || month.equals(Month.DECEMBER) )
		        			dates.add(date);
						break;
					case Quarterly:
						if ( month.equals(Month.MARCH) || month.equals(Month.JUNE) || 
			        				month.equals(Month.SEPTEMBER) || month.equals(Month.DECEMBER) )
			        		dates.add(date);
						break;
					case Monthly:
		        		dates.add(date);
						break;
					case Weekly:
		        		dates.add(date);
						break;
					default:
						break;
				}
			}
	    }
	    
	    return dates;
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
			Logger.log().info("Частота ребалансировки активов: " + rebalType.getFrequency());
			
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
