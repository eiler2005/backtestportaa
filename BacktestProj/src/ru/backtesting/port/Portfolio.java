package ru.backtesting.port;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.patriques.output.timeseries.TimeSeriesResponse;
import org.patriques.output.timeseries.data.StockData;

import ru.backtesting.rebalancing.Frequency;
import ru.backtesting.rebalancing.RebalancingMethod;
import ru.backtesting.rebalancing.RebalancingType;
import ru.backtesting.signal.SignalActionContext;
import ru.backtesting.signal.SignalTestingAction;
import ru.backtesting.stockquotes.StockConnector;
import ru.backtesting.stockquotes.StockQuote;
import ru.backtesting.stockquotes.StockQuoteHistory;
import ru.backtesting.utils.Logger;
import ru.backtesting.utils.PortfolioUtils;

@SuppressWarnings("deprecation")
public class Portfolio {
	public static final String CASH_TICKER = "CASH";
	private String name;
	private int startYear;
	private int endYear;
	private final int initialAmount;
	private final RebalancingType rebalType;
	private final List<AssetAllocation> assetsAllocation;
	private LinkedHashMap<LocalDateTime, List<PositionInformation>> postionsOnDates;
    private boolean reinvestDividends = false;
    private List<SignalTestingAction> timingSignals;
	private String outOfMarketPosTicker;
    
	public Portfolio(String name, List<AssetAllocation> assetsAllocation, int startYear, int endYear, int initialAmount,
			RebalancingType rebalancing, List<SignalTestingAction> timingSignals, boolean reinvestDividends) {
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
			RebalancingType rebalancing, List<SignalTestingAction> timingSignals, String outOfMarketTicker, boolean reinvestDividends) {
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
		this.outOfMarketPosTicker = outOfMarketTicker;
	}

	public void fillQuotesData() {		
		// портфель забивается данными - без расчета цены и т п
		for (AssetAllocation asset : assetsAllocation) {
			String ticker = asset.getTicker();
			
			fillQuotesData(ticker, postionsOnDates);
		}
		
		if (outOfMarketPosTicker != null && !outOfMarketPosTicker.equals(CASH_TICKER)) {
			if ( StockQuoteHistory.storage().containsDataInStorage(outOfMarketPosTicker) )
				return;
			
			StockQuoteHistory.storage().fillQuotesData(outOfMarketPosTicker, startYear, endYear);
		}
	}
	
	private void fillQuotesData(String ticker, LinkedHashMap<LocalDateTime, List<PositionInformation>> positions) {
		if ( StockQuoteHistory.storage().containsDataInStorage(ticker) )
			return;
		
		StockQuoteHistory.storage().fillQuotesData(ticker, startYear, endYear);

		List<LocalDateTime> dates = getTradingDates(ticker, startYear, endYear, rebalType.getFrequency());
		
		for (LocalDateTime date : dates)
			if ( positions.get(date) == null ) {
				List<PositionInformation> otherPositions = new ArrayList <PositionInformation> ();
				otherPositions.add(new PositionInformation(ticker, date));
					
				positions.put(date, otherPositions);
			}
			else {
				List<PositionInformation> otherPositions = positions.get(date);
				otherPositions.add(new PositionInformation(ticker, date));
			}
	}
	
	@Deprecated
	public List<LocalDateTime> getTradingDates(String ticker, int startYear, int endYear, Frequency period) {
		TimeSeriesResponse response = null;
		
		if (period.equals(Frequency.Weekly))
			response = StockConnector.weekly(ticker);
		else
			response = StockConnector.monthly(ticker);

	    List<StockData> stockData = response.getStockData();
		
	    Collections.reverse(stockData);
	    	    	
	    List<LocalDateTime> dates = new ArrayList<LocalDateTime>();
	    
	    dates.add(StockQuoteHistory.storage().getFirstTradedDay(ticker, startYear));
	    
	    // ДОДЕЛАТЬ - например, для Annually считает позицию в июле 2018ого только на декабрь 2017 - а нужно на июнь 2018
	    for (int i = 0; i < stockData.size(); i++) {
			LocalDateTime date = stockData.get(i).getDateTime();
			
			if (date.getYear() >= startYear && date.getYear() <= endYear ) {
				Month month = date.getMonth();
				
				if ( i == 0 || (i == dates.size() - 1) )
					dates.add(date);
				else
				switch(period) {
					case Annually:				
			        	if (month.equals(Month.DECEMBER) )
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
	    
	    // dates.add(stockData.get(stockData.size()-1).getDateTime());
	    
	    return dates;
	}
	
	public void backtestPortfolio() {
	    Logger.log().info(prinfPortfolioInformation());
		
		// BuyAndHold: купил в начале срока - продал в конце
	    if (rebalType.getRebalMethod().equals(RebalancingMethod.BuyAndHold)) {			
			// купить по первой дате
			
			// currPortfolioPrice = PortfolioUtils.buyPortfolio(postionsOnDates.get(key), assetsAllocation, initialAmount, reinvestDividends);
			
			// продать по последней
			
			// разница - профит
			
			// ------------- ДОДЕЛАТЬ
	    	
	    	throw new RuntimeException("Для портфелей с типом " + rebalType.getRebalMethod() + " пока нет реализации");

	    }
	    else if (rebalType.getRebalMethod().equals(RebalancingMethod.AssetProportion)) {			
	    	LocalDateTime prevDate = null;	    	
	    	
	    	// обход по датам
	    	for (LocalDateTime date: postionsOnDates.keySet() ) {
				Logger.log().info("|| || Формирование портфеля на дату: " + date + " || ||");

				List<PositionInformation> positions = postionsOnDates.get(date);
					
    			Logger.log().info("Начинаем пересчитывать стоимость позиций в портфеле");

				double portfolioBalance = prevDate == null ? initialAmount : PortfolioUtils.calculateAllPositionsBalance(postionsOnDates.get(prevDate), date, reinvestDividends, true);
				
    			Logger.log().info("Пересчитали стоимость портфеля на дату [" + date + "](сколько денег у нас есть для покупки): " + Logger.log().doubleLog(portfolioBalance));
				
	    		for (int i = 0; i < assetsAllocation.size(); i++) {
		    		String ticker = assetsAllocation.get(i).getTicker();
		    		
		    		PositionInformation position = positions.get(i);
		    				    		
		    		// надо принять решение покупаем текущую акцию или уходим в outOfMarketTicker
		    		
		    		boolean isHoldInPortfolio = PortfolioUtils.isHoldInPortfolio(timingSignals, ticker, position.getTime());
		    				    		
	    			Logger.log().info("Приняли решение держать в портфеле(true)/продавать(false) [" + ticker + "] : " + isHoldInPortfolio);
		    		
		    		
		    		// для кэша все остается без изменений
		    		double quote = 0, quantity = 1;
		    				    		
		    		// если предыдущая позиция не кеш или первый прогон
		    		if ( prevDate == null || !postionsOnDates.get(prevDate).get(i).getTicker().equals(CASH_TICKER) || isHoldInPortfolio) {
		    			// купить в соответствии с assetAllocation
		    			// считаем сколько стоит акция на данный момент времени
		    			quote = StockQuoteHistory.storage().getQuoteValueByDate(ticker, position.getTime(), reinvestDividends);
	    			
		    			// считаем сколько мы можем купить акций по цене на данный момент времени с учетом текущей стоимости портфеля
		    			quantity = PortfolioUtils.calculateQuantityStocks(ticker, quote, portfolioBalance, assetsAllocation);
		    		
		    			// у нас есть на новые покупки quantity*quote
		    		}
		    		// если кэш
		    		else {		    			
		    			quote = assetsAllocation.get(i).getAllocationPercent()*portfolioBalance/100;
		    			
		    			quantity = 1;
		    		}
		    		
		    		if ( isHoldInPortfolio ) {
						Logger.log().info("Купили в портфель [" + ticker + "] " + Logger.log().doubleLog(quantity) + " лотов на сумму " + Logger.log().doubleLog(quantity*quote) + 
								", цена лота: " + Logger.log().doubleLog(quote) );
		    			
		    			position.buy(quantity, quantity*quote);
		    		} else { // купить в соответствии с outOfMarketTicket и других аллокаций
		    			// перекладываем текущую позицию в outOfMarketPos		    			
		    			Logger.log().info("Перекладываемся в hedge-актив " + outOfMarketPosTicker + " вместо " + ticker + " на дату " + date);
		    			
		    			PositionInformation hegdePos = new PositionInformation(outOfMarketPosTicker, date);
		    			
		    			if ( outOfMarketPosTicker.equals(CASH_TICKER)) {
			    			hegdePos.buy(1, quote*quantity);
			    			
			    			Logger.log().info("Закрыли позицию и вышли в hedge-актив [" + outOfMarketPosTicker + "] на сумму " + Logger.log().doubleLog(quote*quantity));
		    			}
		    			else {
		    				double hedgeQuote = StockQuoteHistory.storage().getQuoteValueByDate(outOfMarketPosTicker, position.getTime(), reinvestDividends);
		    				
		    				double hedgeQuantity = assetsAllocation.get(i).getAllocationPercent()*portfolioBalance/hedgeQuote/100;
		    				
			    			hegdePos.buy(hedgeQuantity, hedgeQuantity*hedgeQuote);
			    			
			    			Logger.log().info("Зашли в hedge-актив [" + outOfMarketPosTicker + "]");
			    			
			    			Logger.log().info("Купили в портфель [" + outOfMarketPosTicker + "] " + Logger.log().doubleLog(hedgeQuantity) + " лотов на сумму " + 
			    					Logger.log().doubleLog(hedgeQuantity*hedgeQuote) + ", цена лота: " + Logger.log().doubleLog(hedgeQuote) );
		    			}
		    			
		    			positions.set(i, hegdePos);	    			
		    		}
		    	}
	    		
				double newPortfolioBalance = PortfolioUtils.calculateAllPositionsBalance(positions); 
	    		
				Logger.log().info("Стоимость портфеля на [" + date + "] : " + Logger.log().doubleLog(newPortfolioBalance));
				
    			Logger.log().info("Информация по позициям нового портфеля ниже:");
    			
    			PortfolioUtils.printPositions(postionsOnDates.get(date));
				
				Logger.log().info("-------------");
	    		
	    		prevDate = date;
	    	}
	    }
	    else if (rebalType.getRebalMethod().equals(RebalancingMethod.ForSignals)) {
	    	// вместо распределения ограничиваем риски макс позицией в том или ином инструменте - скорее риск-менеджмент, а не распределение
	    	
	    	// по frequency перекладываемся в сигналы в соответствии с риском asset alloc
	    	
	    	// если нет сигнала, то в защитный актив
	    	
	    	// мощно перекладыываться c частотой раз в день - не чаще
	    	
			// ------------- ДОДЕЛАТЬ - нужно ли
	    	
	    	throw new RuntimeException("Для портфелей с типом " + rebalType.getRebalMethod() + " пока нет реализации");
		}	    
	}
	
	private String prinfPortfolioInformation() {
		String inf = "";
		
		if (rebalType.getRebalMethod().equals(RebalancingMethod.BuyAndHold)) {
			inf += "Портфель типа Buy&Hold, название : " + name + "\n";
		} else if (rebalType.getRebalMethod().equals(RebalancingMethod.AssetProportion)) {
			inf += "Портфель типа TimingPortfolio с ребалансировкой активов по пропорциям, название " + name+  "\n";
			inf += "Частота ребалансировки активов: " + rebalType.getFrequency() + "\n";
			inf += "Распределение активов: " + assetsAllocation + "\n";
			inf += "Инвестирование дивидендов: " + reinvestDividends + "\n";
			
			if (outOfMarketPosTicker != null)
				inf += "Название hedge-актива при медвежьих рынках или срабатывании сигналов: " + outOfMarketPosTicker + "\n";
		} else if (rebalType.getRebalMethod().equals(RebalancingMethod.ForSignals)) {
			inf += "Портфель типа ForSignals с ребалансировкой активов по сигналам, название " + name + "\n";
			inf += "Частота ребалансировки активов: " + rebalType.getFrequency() + "\n";
			inf +="Распределение активов: " + assetsAllocation + "\n";
			inf += "Инвестирование дивидендов: " + reinvestDividends + "\n";
			
			if (outOfMarketPosTicker != null)
				inf += "Название hedge-актива при медвежьих рынках или срабатывании сигналов: " + outOfMarketPosTicker + "\n";
		}
		
		return inf;
	}
	
	private boolean haveTimingSignals() {
		return timingSignals != null && timingSignals.size() != 0;
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

	public void printAllPosiotions() {
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

	public String getName() {
		return name;
	}

	public LinkedHashMap<LocalDateTime, List<PositionInformation>> getPostionsOnDates() {
		return postionsOnDates;
	}

	public String getOutOfMarketPosTicker() {
		return outOfMarketPosTicker;
	}
	
	public Set<String> getAllTickersInPort() {
		Set<String> tickers = new HashSet<String>();
		for (int i = 0; i < assetsAllocation.size(); i++) {
    		String ticker = assetsAllocation.get(i).getTicker();
    		
    		tickers.add(ticker);
		}
		
		if ( outOfMarketPosTicker != null && !outOfMarketPosTicker.equals(CASH_TICKER) )
			tickers.add(outOfMarketPosTicker);
		
		return tickers;
	}
}
