package ru.backtesting.stockquotes;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
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
	private HashMap<String, List<LocalDateTime>> allDates;
		
	private StockQuoteHistory() {
		allDates = new HashMap<String, List<LocalDateTime>>();
		quotes = new HashMap<String, List<StockQuote>>();
	}
	
	public static synchronized StockQuoteHistory storage() {
		if (instance == null) {
			instance = new StockQuoteHistory();
		}
		
		return instance;
	}
	
	
	private String getKey(String ticker, TradingTimeFrame period) {
		return "[" + ticker + "] , [" + period + "]";
	}
	
	public List<LocalDateTime> fillQuotesData(String ticker, TradingTimeFrame period) {
		String tickerKey = getKey(ticker, period);
		
		if ( containsDataInStorage(ticker, period) )
			return allDates.get(tickerKey);
			
		TimeSeriesResponse response = null;
		
		if (period.equals(TradingTimeFrame.Daily))
			response = StockConnector.daily(ticker);
		else if (period.equals(TradingTimeFrame.Weekly))
			response = StockConnector.weekly(ticker);
		else if (period.equals(TradingTimeFrame.Monthly))
			response = StockConnector.monthly(ticker); 

	    List<StockData> stockData = response.getStockData();
		
	    Collections.reverse(stockData);
	    	    	   	    
	    for (StockData stock : stockData) {	
	    	LocalDateTime dateTime = stock.getDateTime();
	    		    	
	    	StockQuote quote = new StockQuote(ticker, dateTime, stock.getOpen(), 
	    			stock.getClose(), stock.getAdjustedClose(), stock.getHigh(), stock.getLow(), stock.getDividendAmount());   	
	    		 
	    	// add all dates
    		if (allDates.get(tickerKey) != null) {
	    		allDates.get(tickerKey).add(dateTime);
	    	}
	    	else {
	    		List<LocalDateTime> list = new ArrayList<LocalDateTime>();
	    		list.add(dateTime);
	    		allDates.put(tickerKey, list);
	    	}	 
    		
    		if (quotes.get(tickerKey) != null)
    			quotes.get(tickerKey).add(quote);
    		else {
    			List<StockQuote> list = new ArrayList<StockQuote>();
    			list.add(quote);
    			quotes.put(tickerKey, list);		    	
    		}
	    }
	    	    
	    return allDates.get(tickerKey);
	}
	
	@Deprecated
	private List<LocalDateTime> getDatesByYearFilter(String ticker, TradingTimeFrame period, int startYear, int endYear) {
		String tickerKey = getKey(ticker, period);
		
		List<LocalDateTime> datesByYearFilter = new ArrayList<LocalDateTime>();
		
		if ( containsDataInStorage(ticker, period) )
			datesByYearFilter = DateUtils.filterDateListByYear(allDates.get(tickerKey), startYear, endYear);
		
		if ( datesByYearFilter.size() == 0 )
			throw new AlphaVantageException("По активу [" + ticker + "] не найдены котировки в промежутке между следующими годами [" + startYear + ", " + endYear + "]. "
				+ "Возможно, данных на указанную дату не существует в хранилище www.alphavantage.com или они не загружены");
		else
			return datesByYearFilter;
	}
	
	public List<LocalDateTime> getQuoteDatesForTicker(String ticker, TradingTimeFrame period) {
		return allDates.get(getKey(ticker, period));
	}
	
	public double getQuoteValueByDate(String ticker, TradingTimeFrame period, LocalDateTime date, boolean dividends) {
		if (dividends)
			return getQuoteByDate(ticker, period, date).getAdjustedClose();
		else
			return getQuoteByDate(ticker, period, date).getClose();
	}
	
	public List<Double> getQuoteValuesByDates(String ticker, TradingTimeFrame period, List<LocalDateTime> dates, boolean dividens) {
		List<Double> quoteValues = new ArrayList<Double>();
		
		for (LocalDateTime date : dates) {			
			double quoteValue = getQuoteValueByDate(ticker, period, date, dividens);
			
			quoteValues.add(quoteValue);			
		}
		
		return quoteValues;
	}
	
	public StockQuote getQuoteByDate(String ticker, TradingTimeFrame period, LocalDateTime date) {
		List<StockQuote> list = quotes.get(getKey(ticker, period));
				
		for (StockQuote q : list)
			if ( DateUtils.compareDatesByDay(q.getTime(), date) ) 
				return q;
		
		// попытаемся поискать в других периодах значения котировки акции
		List<TradingTimeFrame> periods = Arrays.asList(new TradingTimeFrame[] 
				{ TradingTimeFrame.Daily, TradingTimeFrame.Weekly, TradingTimeFrame.Monthly });
		
		for ( TradingTimeFrame curPeriod : periods  )
			if ( !curPeriod.equals(period) && containsDataInStorageOnDate(ticker, curPeriod, date))			
				return getQuoteByDate(ticker, curPeriod, date);
		
		
		throw new AlphaVantageException("По активу [" + ticker + "] на дату " + date + " не загружены котировки. "
				+ "Возможно, данных на указанную дату не существует в хранилище www.alphavantage.co");
	}
	
	public LocalDateTime getFirstTradingDayInYear(String ticker, TradingTimeFrame period, int startYear) {
		List<LocalDateTime> datesQ = allDates.get(getKey(ticker, period));
		
		for (LocalDateTime date : datesQ) {
			if (date.getYear() == startYear && date.getMonth().equals(Month.JANUARY) )
					return date;
		}
		
		return null;
	}
	
	public LocalDateTime getLastTradingDayInYear(String ticker, TradingTimeFrame period, int year) {
		List<LocalDateTime> datesQ = allDates.get(getKey(ticker, period));
		
		Collections.reverse(datesQ);
		
		for (LocalDateTime date : datesQ)
			if (date.getYear() == year )
				return date;
		
		return null;
	}
	
	public boolean containsDataInStorage(String ticker, TradingTimeFrame period) {
		String tickerKey = getKey(ticker, period);
		
		return quotes.containsKey(tickerKey) && allDates.containsKey(tickerKey);
	}
	
	public boolean containsDataInStorageOnDate(Set<String> tickers, TradingTimeFrame period, LocalDateTime date) {
		for (String ticker : tickers)
			if ( !containsDataInStorageOnDate(ticker, period, date) )
				return false;
		
		return true;
	}
	
	public boolean containsDataInStorageOnDate(String ticker, TradingTimeFrame period, LocalDateTime date) {
		List<StockQuote> list = quotes.get(getKey(ticker, period));

		for (StockQuote q : list)
			if ( DateUtils.compareDatesByDay(q.getTime(), date) ) 
				return true;
		
		return false;		
	}

	public List<LocalDateTime> getTradingDatesByPeriod(String ticker, TradingTimeFrame period) {
		String tickerKey = getKey(ticker, period);

		if (allDates.containsKey(tickerKey))
			return allDates.get(tickerKey);
		else
			throw new AlphaVantageException(
					"Необходимо загрузить данные для тикера " + ticker + " и периода " + period);
	}
	
	public List<LocalDateTime> getTradingDatesByFilter(String ticker, TradingTimeFrame period, int startYear, int endYear, Frequency frequency) {
		if ( !containsDataInStorage(ticker, period) )
			throw new AlphaVantageException(
					"Необходимо загрузить данные для тикера " + ticker + " и периода " + period);
						
		if ( period == TradingTimeFrame.Daily )
			throw new RuntimeException("Некорректно указан период TradingPeriod. Нельзя указывать TradingPeriod = Daily. "
					+ "Это приведет к некорретному расчету портфеля каждый день, что не приведет к хорошим результатам");
		
		// dates.add(getFirstTradedDay(ticker, startYear));

		List<LocalDateTime> filteredDates = new ArrayList<LocalDateTime>();

		List<LocalDateTime> frequencyDates = getTradingDatesByPeriod(ticker, period);
		
		// ДОДЕЛАТЬ - например, для Annually считает позицию в июле 2018ого только на
		// декабрь 2017 - а нужно на июнь 2018
		for (int i = 0; i < frequencyDates.size(); i++) {
			LocalDateTime date = frequencyDates.get(i);

			if (date.getYear() >= startYear && date.getYear() <= endYear) {
				Month month = date.getMonth();

				if (i == 0 || (i == (frequencyDates.size() - 1)))
					filteredDates.add(date);
				else
					switch (frequency) {
					case Annually:
						if (month.equals(Month.DECEMBER))
							filteredDates.add(date);
						break;
					case SemiAnnually:
						if (month.equals(Month.JUNE) || month.equals(Month.DECEMBER))
							filteredDates.add(date);
						break;
					case Quarterly:
						if (month.equals(Month.MARCH) || month.equals(Month.JUNE) || month.equals(Month.SEPTEMBER)
								|| month.equals(Month.DECEMBER))
							filteredDates.add(date);
						break;
					case Monthly:
						filteredDates.add(date);
						break;
					case Weekly:
						filteredDates.add(date);
						break;
					default:
						filteredDates.add(date);
						break;
					}
			}
		}

		if (filteredDates.size() == 0)
			throw new AlphaVantageException("По активу [" + ticker + 
					"] не найдены котировки в промежутке между следующими годами [" + startYear + ", " + endYear  + "]. " +
					"Возможно, данных на указанную дату не существует в хранилище www.alphavantage.com или они не загружены");

		// вставляем последний день года при Frequency.Annually
		if (frequency.equals(Frequency.Annually))
			filteredDates.add(frequencyDates.get(frequencyDates.size() - 1));

		return filteredDates;
	}
}
