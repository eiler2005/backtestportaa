package ru.backtesting.stockquotes;

import java.util.HashMap;

import org.patriques.AlphaVantageConnector;
import org.patriques.TechnicalIndicators;
import org.patriques.TimeSeries;
import org.patriques.input.technicalindicators.Interval;
import org.patriques.input.technicalindicators.NBDevDn;
import org.patriques.input.technicalindicators.NBDevUp;
import org.patriques.input.technicalindicators.SeriesType;
import org.patriques.input.technicalindicators.TimePeriod;
import org.patriques.output.AlphaVantageException;
import org.patriques.output.technicalindicators.TechnicalIndicatorResponse;
import org.patriques.output.timeseries.Daily;
import org.patriques.output.timeseries.TimeSeriesResponse;

import ru.backtesting.utils.Logger;

public class StockConnector {
	final static String apiKey1_full = "2ARLX4ESX0694J0T"; 
	
	final static String apiKey2 = "BUUEKHHHWG6ITPI1";
	final static String apiKey3 = "H1QJ7U0335G7SURS";
	final static String apiKey4 = "9DQHRPQNKEW4V3LV";
	final static String apiKey5 = "7I3MD9RX85YI6KNU";

	static int timeout = 5000;
	private static AlphaVantageConnector fullConn;

	private static int fullConnCount = 0;
	private static HashMap<String, AlphaVantageConnector> connPool;

	
	public static Daily daily(String ticker) {
		fullConnCount++;
		
		Logger.log().info("Использую подключение к AlphaVantage типа full, счетчик вызовов: " + fullConnCount);
		
		return new TimeSeries(fullConn()).daily(ticker);
	}
	
	public static TimeSeriesResponse weekly(String ticker) {	
				
		return new TimeSeries(connFromPool()).weekly(ticker);
	}
	
	public static TimeSeriesResponse monthly(String ticker) {		
		
		return new TimeSeries(connFromPool()).monthly(ticker);
	}
	
	public static TechnicalIndicatorResponse sma(String symbol, Interval interval, TimePeriod timePeriod, SeriesType seriesType) {
		return new TechnicalIndicators(connFromPool()).sma(symbol, interval, timePeriod, seriesType);
	}
	
	// weighted moving average 
	public static TechnicalIndicatorResponse wma(String symbol, Interval interval, TimePeriod timePeriod, SeriesType seriesType) {
		return new TechnicalIndicators(connFromPool()).wma(symbol, interval, timePeriod, seriesType);
	}
	
	public static TechnicalIndicatorResponse ema(String symbol, Interval interval, TimePeriod timePeriod, SeriesType seriesType) {
		return new TechnicalIndicators(connFromPool()).ema(symbol, interval, timePeriod, seriesType);
	}
	
	public static TechnicalIndicatorResponse rsi(String symbol, Interval interval, TimePeriod timePeriod, SeriesType seriesType) {
		
		return new TechnicalIndicators(connFromPool()).rsi(symbol, interval, timePeriod, seriesType);
	}
	
	public static TechnicalIndicatorResponse cmo(String symbol, Interval interval, TimePeriod timePeriod, SeriesType seriesType) {	
		return new TechnicalIndicators(connFromPool()).cmo(symbol, interval, timePeriod, seriesType);
	}
	
	public static TechnicalIndicatorResponse bbands(String symbol, Interval interval, TimePeriod timePeriod, SeriesType seriesType) {	
		return new TechnicalIndicators(connFromPool()).bbands(symbol, interval, timePeriod, seriesType, NBDevUp.of(2), NBDevDn.of(2), null);
	}
	
	private static AlphaVantageConnector fullConn() {
		try {			
			if (fullConn == null) {
				fullConn = new AlphaVantageConnector(apiKey1_full + "&outputsize=full", timeout);
			}
			
			return fullConn;
		} catch (AlphaVantageException e) {
			
	      System.out.println("something went wrong: AlphaVantageException in connection");
	      e.printStackTrace();
	      
	      throw e;
	    }
	}
	
	@Deprecated
	public static AlphaVantageConnector conn() {
		/*try {
			
			if (conn == null) {
				conn = new AlphaVantageConnector(apiKey2, timeout);
			}
			
			return conn;
		} catch (AlphaVantageException e) {
	      System.out.println("something went wrong:");
	      e.printStackTrace();
	      
	      return null;
	    }*/
		
		return connFromPool();
	}
	
	private static AlphaVantageConnector connFromPool() {
		if ( connPool == null ) {
			connPool = new HashMap<String, AlphaVantageConnector>();
			
			connPool.put(apiKey2, new AlphaVantageConnector(apiKey2, timeout));
			connPool.put(apiKey3, new AlphaVantageConnector(apiKey3, timeout));
			connPool.put(apiKey4, new AlphaVantageConnector(apiKey4, timeout));
			connPool.put(apiKey5, new AlphaVantageConnector(apiKey5, timeout));

		}
		
		for (String apiKey : connPool.keySet()) {
			AlphaVantageConnector connection = connPool.get(apiKey);
			
			if (connection.getConnCount() <= 4) {
				connection.countPlus();
				
				Logger.log().info("Использую подключение к www.alphavantage.co из пула[apiKey - " + connection.getapiKey() + 
						"], счетчик вызовов: " + connection.getConnCount());	
				
				waitForFreeAccess();
				
				return connection;
			}
		}
		
		throw new AlphaVantageException("Закончились соединения с www.alphavantage.co в пуле");
	}
	
	// 5 resuest per 1 minute - free
	private static void waitForFreeAccess() {
        try {
        	
        	int i = 13;
        	
        	Logger.log().info("Ждем " + i + " секунд из-за ограничений на 5 подключений в минуту к сайту www.alphavantage.co");
			
        	Thread.sleep(i*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
	}
}
