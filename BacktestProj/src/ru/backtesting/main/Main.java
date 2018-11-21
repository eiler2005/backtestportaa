package ru.backtesting.main;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.patriques.ForeignExchange;
import org.patriques.SectorPerformances;
import org.patriques.TechnicalIndicators;
import org.patriques.TimeSeries;
import org.patriques.input.technicalindicators.Interval;
import org.patriques.input.technicalindicators.SeriesType;
import org.patriques.input.technicalindicators.TimePeriod;
import org.patriques.output.AlphaVantageException;
import org.patriques.output.exchange.CurrencyExchange;
import org.patriques.output.exchange.data.CurrencyExchangeData;
import org.patriques.output.sectorperformances.Sectors;
import org.patriques.output.sectorperformances.data.SectorData;
import org.patriques.output.technicalindicators.SMA;
import org.patriques.output.technicalindicators.data.IndicatorData;
import org.patriques.output.timeseries.MonthlyAdjusted;

import ru.backtesting.stockquotes.StockConnector;
import ru.backtesting.techindicators.SMAType;
import ru.backtesting.techindicators.TechIndicatorsHistory;

// https://github.com/patriques82/alphavantage4j

/* technical ind library

https://github.com/ishanthilina/TA-Lib-Java-Examples  тех индикаторы
https://stooq.com/q/d/?s=usdrub - курсы валют в cvs файл за много лет
https://github.com/zavtech/morpheus-core/blob/master/README.md графики

https://iextrading.com/developer/docs/#getting-started

https://news.ycombinator.com/item?id=14546565 - про фин библиотеки
*/

public class Main {
	
	public static void main(String[] args) {
		MonthlyAdjusted response = new TimeSeries(StockConnector.conn()).monthlyAdjusted("SPY");
		
		/*
	    Map<String, String> metaData = response.getMetaData();
	    System.out.println("Information: " + metaData.get("1. Information"));
	    System.out.println("Stock: " + metaData.get("2. Symbol"));
	    System.out.println("Last Refreshed: " + metaData.get("3. Last Refreshed"));

	      
	    List<StockData> stockData = response.getStockData();
	    
	    stockData = rotate(stockData);
	    
	    for (int i = 0; i < stockData.size(); i++) {
	      StockData stock = stockData.get(i);
	      System.out.println("date:   " + stock.getDateTime());
	      System.out.println("open:   " + stock.getOpen());
	      System.out.println("high:   " + stock.getHigh());
	      System.out.println("low:    " + stock.getLow());
	      System.out.println("close:  " + stock.getClose());
	      System.out.println("volume: " + stock.getVolume());
	     }*/
	    
	    // foreignExchangeSample();
	    
	   //  techIndicator();
	    
		TechIndicatorsHistory.storage().fillSMAData("SPY", SMAType.TwoHundredDays);
		
	    // sectorPerfomances();
	}
	
	public static void foreignExchangeSample() {
	    ForeignExchange foreignExchange = new ForeignExchange(StockConnector.conn());

	    try {
	      CurrencyExchange currencyExchange = foreignExchange.currencyExchangeRate("USD", "SEK");
	      CurrencyExchangeData currencyExchangeData = currencyExchange.getData();

	      System.out.println("from currency code: " + currencyExchangeData.getFromCurrencyCode());
	      System.out.println("from currency name: " + currencyExchangeData.getFromCurrencyName());
	      System.out.println("to currency code:   " + currencyExchangeData.getToCurrencyCode());
	      System.out.println("to currency name:   " + currencyExchangeData.getToCurrencyName());
	      System.out.println("exchange rate:      " + currencyExchangeData.getExchangeRate());
	      System.out.println("last refresh:       " + currencyExchangeData.getTime());
	    } catch (AlphaVantageException e) {
	      System.out.println("something went wrong");
	    }
	  }
	
	public static void techIndicator() {

	    TechnicalIndicators technicalIndicators = new TechnicalIndicators(StockConnector.conn());

	    try {
		  SMA sma = technicalIndicators.sma("SPY", Interval.DAILY, TimePeriod.of(50), SeriesType.CLOSE);
		  
		  System.out.println("Meta data:" + sma.getMetaData());
		  
		  List<IndicatorData> smaList = sma.getData();
		  
		  Collections.reverse(smaList);
		  
		  for (IndicatorData smaData : smaList) {
			  int day = smaData.getDateTime().getDayOfMonth();
			  
			  if ( day == 28 || day == 29 || day == 30 || day == 31 || day == 16)
				  System.out.println("Data:" + smaData.getData() + ", " + smaData.getDateTime());
		  }
	    		  
	    /*	
	      MACD response = technicalIndicators.macd("MSFT", Interval.WEEKLY, TimePeriod.of(10), SeriesType.CLOSE, null, null, null);
	      Map<String, String> metaData = response.getMetaData();
	      System.out.println("Symbol: " + metaData.get("1: Symbol"));
	      System.out.println("Indicator: " + metaData.get("2: Indicator"));

	      List<MACDData> macdData = response.getData();
	      macdData.forEach(data -> {
	        System.out.println("date:           " + data.getDateTime());
	        System.out.println("MACD Histogram: " + data.getHist());
	        System.out.println("MACD Signal:    " + data.getSignal());
	        System.out.println("MACD:           " + data.getMacd());
	      });*/
	    } catch (AlphaVantageException e) {
	      System.out.println("something went wrong");
	      
	      e.printStackTrace();
	    }
	  }
	
	public static void sectorPerfomances() {
	    SectorPerformances sectorPerformances = new SectorPerformances(StockConnector.conn());

	    try {
	      Sectors response = sectorPerformances.sector();
	      Map<String, String> metaData = response.getMetaData();
	      System.out.println("Information: " + metaData.get("Information"));
	      System.out.println("Last Refreshed: " + metaData.get("Last Refreshed"));

	      List<SectorData> sectors = response.getSectors();
	      sectors.forEach(data -> {
	        System.out.println("key:           " + data.getKey());
	        System.out.println("Consumer Discretionary: " + data.getConsumerDiscretionary());
	        System.out.println("Consumer Staples:       " + data.getConsumerStaples());
	        System.out.println("Energy:                 " + data.getEnergy());
	        System.out.println("Financials:             " + data.getFinancials());
	        System.out.println("Health Care:            " + data.getHealthCare());
	        System.out.println("Industrials:            " + data.getIndustrials());
	        System.out.println("Information Technology: " + data.getInformationTechnology());
	        System.out.println("Materials:              " + data.getMaterials());
	        System.out.println("Real Estate:            " + data.getRealEstate());
	      });
	    } catch (AlphaVantageException e) {
	      System.out.println("something went wrong");
	    }
	  }
}
