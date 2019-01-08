package ru.backtesting.stockquotes;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.patriques.output.AlphaVantageException;
import org.patriques.output.timeseries.Daily;
import org.patriques.output.timeseries.TimeSeriesResponse;
import org.patriques.output.timeseries.data.StockData;

import ru.backtesting.rebalancing.Frequency;
import ru.backtesting.utils.DateUtils;

public class StockQuoteHistory {
	private static StockQuoteHistory instance;
	private HashMap<String, List<StockQuote>> quotes;
	private HashMap<String, List<LocalDateTime>> dates;
	
	private HashMap<TradingPeriod, List<LocalDateTime>> tradingDates;
	
	private StockQuoteHistory() {
		dates = new HashMap<String, List<LocalDateTime>>();
		quotes = new HashMap<String, List<StockQuote>>();
		tradingDates = new HashMap<TradingPeriod, List<LocalDateTime>>();
	}
	
	public static synchronized StockQuoteHistory storage() {
		if (instance == null) {
			instance = new StockQuoteHistory();
		}
		
		return instance;
	}
	
	public List<LocalDateTime> fillQuotesData(String ticker, int startYear, int endYear) {
		if ( containsDataInStorage(ticker) )
			return dates.get(ticker);
			
		Daily response = StockConnector.daily(ticker);

	    List<StockData> stockData = response.getStockData();
		
	    Collections.reverse(stockData);
	    	    	    
	    for (StockData stock : stockData) {	
	    	LocalDateTime dateTime = stock.getDateTime();
	    		    	
	    	StockQuote quote = new StockQuote(ticker, dateTime, stock.getOpen(), 
	    			stock.getClose(), stock.getAdjustedClose(), stock.getHigh(), stock.getLow(), stock.getDividendAmount());   	
	    		    	
	    	if (dateTime.getYear() >= startYear && dateTime.getYear() <= endYear) {
	    		
	    		if (dates.get(ticker) != null) {
		    		dates.get(ticker).add(dateTime);
		    	}
		    	else {
		    		List<LocalDateTime> list = new ArrayList<LocalDateTime>();
		    		list.add(dateTime);
		    		dates.put(ticker, list);
		    	}	 
	    		
	    		if (quotes.get(ticker) != null)
	    			quotes.get(ticker).add(quote);
	    		else {
	    			List<StockQuote> list = new ArrayList<StockQuote>();
	    			list.add(quote);
	    			quotes.put(ticker, list);		    	
	    		}	
	    	}
	    }
	    	    
	    return dates.get(ticker);
	}
	
	public List<LocalDateTime> getQuoteDatesForTicker(String ticker) {
		return dates.get(ticker);
	}
	
	public double getQuoteValueByDate(String ticker, LocalDateTime date, boolean dividends) {
		if (dividends)
			return getQuoteByDate(ticker, date).getAdjustedClose();
		else
			return getQuoteByDate(ticker, date).getClose();
	}
	
	public List<Double> getQuoteValuesByDates(String ticker, List<LocalDateTime> dates, boolean dividens) {
		List<Double> quoteValues = new ArrayList<Double>();
		
		for (LocalDateTime date : dates) {			
			double quoteValue = getQuoteValueByDate(ticker, date, dividens);
			
			quoteValues.add(quoteValue);			
		}
		
		return quoteValues;
	}
	
	public StockQuote getQuoteByDate(String ticker, LocalDateTime date) {
		List<StockQuote> list = quotes.get(ticker);
				
		for (StockQuote q : list)
			if ( DateUtils.compareDatesByDay(q.getTime(), date) ) 
				return q;
		
		throw new AlphaVantageException("По активу [" + ticker + " на дату " + date + " не загружены котировки. "
				+ "Возможно, данных на указанную дату не существует в хранилище www.alphavantage.co");
	}
	
	public LocalDateTime getFirstTradedDay(String ticker, int startYear) {
		List<LocalDateTime> datesQ = dates.get(ticker);
		
		for (LocalDateTime date : datesQ) {
			if (date.getYear() == startYear && date.getMonth().equals(Month.JANUARY) )
					return date;
		}
		
		return null;
	}
	
	public boolean containsDataInStorage(String ticker) {
		return quotes.containsKey(ticker) && dates.containsKey(ticker);
	}
	
	public boolean containsDataInStorageOnDate(Set<String> tickers, LocalDateTime date) {
		for (String ticker : tickers)
			if ( !containsDataInStorageOnDate(ticker, date) )
				return false;
		
		return true;
	}
	
	public boolean containsDataInStorageOnDate(String ticker, LocalDateTime date) {
		List<StockQuote> list = quotes.get(ticker);

		for (StockQuote q : list)
			if ( DateUtils.compareDatesByDay(q.getTime(), date) ) 
				return true;
		
		return false;		
	}

	@Deprecated
	public List<LocalDateTime> getTradingDates(String ticker, int startYear, int endYear, Frequency frequency) {
		TradingPeriod tradingPeriod = new TradingPeriod(ticker, startYear, endYear, frequency);
		
		if (tradingDates.containsKey(tradingPeriod))
			return tradingDates.get(tradingPeriod);
		
		TimeSeriesResponse response = null;
		
		if (frequency.equals(Frequency.Daily))
			response = StockConnector.daily(ticker);
		else if (frequency.equals(Frequency.Weekly))
			response = StockConnector.weekly(ticker);
		else
			response = StockConnector.monthly(ticker);
	
	    List<StockData> stockData = response.getStockData();
		
	    Collections.reverse(stockData);
	    	    	
	    List<LocalDateTime> dates = new ArrayList<LocalDateTime>();
	    
	    // dates.add(getFirstTradedDay(ticker, startYear));
	    
	    // ДОДЕЛАТЬ - например, для Annually считает позицию в июле 2018ого только на декабрь 2017 - а нужно на июнь 2018
	    for (int i = 0; i < stockData.size(); i++) {
			LocalDateTime date = stockData.get(i).getDateTime();
			
			if (date.getYear() >= startYear && date.getYear() <= endYear ) {
				Month month = date.getMonth();
				
				if ( i == 0 || (i == (dates.size() - 1) ) )
					dates.add(date);
				else
				switch(frequency) {
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
						dates.add(date);
						break;
				}
			}
	    }
	    
	   if ( frequency.equals(Frequency.Annually) )
	    	dates.add(stockData.get(stockData.size()-1).getDateTime());
	    
	    tradingDates.put(tradingPeriod, dates);
	    
	    return dates;
	}
	
	private final class TradingPeriod {
		private String ticker;
		private int startYear;
		int endYear;
		private Frequency period;
		
		public TradingPeriod(String ticker, int startYear, int endYear, Frequency period) {
			super();
			this.ticker = ticker;
			this.startYear = startYear;
			this.endYear = endYear;
			this.period = period;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getEnclosingInstance().hashCode();
			result = prime * result + Objects.hash(endYear, period, startYear, ticker);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TradingPeriod other = (TradingPeriod) obj;
			if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
				return false;
			return endYear == other.endYear && period == other.period && startYear == other.startYear
					&& Objects.equals(ticker, other.ticker);
		}

		private StockQuoteHistory getEnclosingInstance() {
			return StockQuoteHistory.this;
		}
	}
}
