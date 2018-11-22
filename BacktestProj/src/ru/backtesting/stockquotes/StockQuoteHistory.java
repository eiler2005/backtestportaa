package ru.backtesting.stockquotes;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.patriques.TimeSeries;
import org.patriques.output.timeseries.Daily;
import org.patriques.output.timeseries.data.StockData;

import ru.backtesting.utils.DateUtils;

public class StockQuoteHistory {
	private static StockQuoteHistory instance;
	private HashMap<String, List<StockQuote>> quotes;
	private HashMap<String, List<LocalDateTime>> dates;
	
	private StockQuoteHistory() {
		dates = new HashMap<String, List<LocalDateTime>>();
		quotes = new HashMap<String, List<StockQuote>>();
	}
	
	public static synchronized StockQuoteHistory storage() {
		if (instance == null) {
			instance = new StockQuoteHistory();
		}
		
		return instance;
	}
	
	public List<LocalDateTime> fillQuotesData(String ticker, int startYear, int endYear) {
		Daily response = new TimeSeries(StockConnector.fullConn()).daily(ticker);

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
	
	public StockQuote getQuoteByDate(String ticker, LocalDateTime date) {
		List<StockQuote> list = quotes.get(ticker);
				
		for (StockQuote q : list)
			if ( DateUtils.compareDatesByDay(q.getTime(), date) ) 
				return q;
				
		return null;
	}
	
	public LocalDateTime getFirstTradedDay(String ticker, int startYear) {
		List<LocalDateTime> datesQ = dates.get(ticker);
		
		for (LocalDateTime date : datesQ) {
			if (date.getYear() == startYear && date.getMonth().equals(Month.JANUARY) )
					return date;
		}
		
		return null;
	}
}
