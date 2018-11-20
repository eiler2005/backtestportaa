package ru.backtesting.stockquotes;

import org.patriques.AlphaVantageConnector;
import org.patriques.output.AlphaVantageException;

public class StockConnector {
	final static String apiKey1 = "2ARLX4ESX0694J0T"; 
	final static String apiKey2 = "BUUEKHHHWG6ITPI1"; 

	static int timeout = 3000;
	private static AlphaVantageConnector conn;
	private static AlphaVantageConnector fullConn;

	
	public static AlphaVantageConnector fullConn() {
		try {
			if (fullConn == null) {
				fullConn = new AlphaVantageConnector(apiKey1 + "&outputsize=full", timeout);
			}
			
			return fullConn;
		} catch (AlphaVantageException e) {
	      System.out.println("something went wrong:");
	      e.printStackTrace();
	      
	      return null;
	    }
	}
	
	public static AlphaVantageConnector conn() {
		try {
			if (conn == null) {
				conn = new AlphaVantageConnector(apiKey2, timeout);
			}
			
			return conn;
		} catch (AlphaVantageException e) {
	      System.out.println("something went wrong:");
	      e.printStackTrace();
	      
	      return null;
	    }
	}
}
