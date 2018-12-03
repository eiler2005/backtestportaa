package ru.backtesting.stockindicators;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.patriques.TechnicalIndicators;
import org.patriques.input.technicalindicators.Interval;
import org.patriques.input.technicalindicators.SeriesType;
import org.patriques.input.technicalindicators.TimePeriod;
import org.patriques.output.AlphaVantageException;
import org.patriques.output.technicalindicators.BBANDS;
import org.patriques.output.technicalindicators.RSI;
import org.patriques.output.technicalindicators.SMA;
import org.patriques.output.technicalindicators.TechnicalIndicatorResponse;
import org.patriques.output.technicalindicators.data.BBANDSData;
import org.patriques.output.technicalindicators.data.IndicatorData;

import ru.backtesting.stockquotes.StockConnector;
import ru.backtesting.utils.DateUtils;
import ru.backtesting.utils.Logger;


public class StockIndicatorsHistory {
	public final static String SMA_IND_ID = "SMA";
	public final static String RSI_OSC_ID = "RSI";
	public final static String CHANDE_MOMENTUM_OSC_ID = "CMO";
	public final static String BOLLINGER_BANDS_ID = "bbands";
	
	private static StockIndicatorsHistory instance;
	private static HashMap<String, HashMap<Integer, List<Object>>> indicatorsStorage;

	
	private StockIndicatorsHistory() {
		indicatorsStorage = new HashMap<String, HashMap<Integer, List<Object>>>();
	}
	
	public static synchronized StockIndicatorsHistory storage() {
		if (instance == null) {
			instance = new StockIndicatorsHistory();
		}
		
		return instance;
	}
	
	private String generateKeyForIndTicker(String ticker, int timePeriod, String indicator) {
		return ticker + String.valueOf(timePeriod) + indicator; 
	}
	
	private List<LocalDateTime> fillIndicatosData(String ticker, int timePeriod, String indicator) {
		String smaStorageKey = generateKeyForIndTicker(ticker, timePeriod, indicator);
		
		if ( !indicatorsStorage.containsKey(smaStorageKey) || !indicatorsStorage.get(smaStorageKey).containsKey(new Integer(timePeriod)) )
			try {			 
				 
				List<Object> indList = null;
				TechnicalIndicatorResponse resp = null;
				 
				if (indicator.equals(SMA_IND_ID)) {
					resp = StockConnector.sma(ticker, Interval.DAILY, TimePeriod.of(timePeriod), SeriesType.CLOSE);;
				} else if (indicator.equals(RSI_OSC_ID)) {
					resp = StockConnector.rsi(ticker, Interval.DAILY, TimePeriod.of(timePeriod), SeriesType.CLOSE);
				} else if (indicator.equals(CHANDE_MOMENTUM_OSC_ID)) {
					resp = StockConnector.cmo(ticker, Interval.DAILY, TimePeriod.of(timePeriod), SeriesType.CLOSE);
				} else if (indicator.equals(BOLLINGER_BANDS_ID)) {
					resp = StockConnector.bbands(ticker, Interval.DAILY, TimePeriod.of(timePeriod), SeriesType.CLOSE);
				}
				else
					throw new RuntimeException("Индикатор " + indicator + " не найден для тикера " + ticker + "и периода " + timePeriod);
				
				indList = resp.getData();

				HashMap<Integer, List<Object>> indicatorDates = new HashMap<Integer, List<Object>>();
				  
				Collections.reverse(indList);
				 
				indicatorDates.put(new Integer(timePeriod), indList);
				 			 
				indicatorsStorage.put(smaStorageKey,indicatorDates);
				 
				Logger.log().info("Загрузка в хранилище индикатора " + indicator + ": " + resp.getMetaData());
			 } catch (AlphaVantageException e) {
			      throw e;
			 } 
		
		List<LocalDateTime> dates = new ArrayList<LocalDateTime>();
		
		for (Object indData : indicatorsStorage.get(smaStorageKey).get(new Integer(timePeriod))) {
			LocalDateTime date = null;
			
			if ( indData instanceof IndicatorData)
				date = ((IndicatorData) indData).getDateTime();
			else if (indData instanceof BBANDSData  ) 
				date = ((BBANDSData) indData).getDateTime();
				
			if ( !dates.contains(date) )
				dates.add(date);
		}
		 
		 return dates;
	}
	
	public List<LocalDateTime> fillSMAData(String ticker, int timePeriod) {
		 return fillIndicatosData(ticker, timePeriod, SMA_IND_ID) ;
	}
	
	public List<LocalDateTime> fillRSIData(String ticker, int timePeriod) {
		 return fillIndicatosData(ticker, timePeriod, RSI_OSC_ID) ;
	}
	
	public List<LocalDateTime> fillCMOData(String ticker, int timePeriod) {
		 return fillIndicatosData(ticker, timePeriod, CHANDE_MOMENTUM_OSC_ID) ;
	}
	
	public List<LocalDateTime> fillBBandsData(String ticker, int timePeriod) {
		 return fillIndicatosData(ticker, timePeriod, BOLLINGER_BANDS_ID) ;
	}
	
	private List<Object> findIndicatorData(String ticker, int timePeriod, String indID) {
		String smaStorageKey = generateKeyForIndTicker(ticker, timePeriod, indID);

		HashMap<Integer, List<Object>> indicatorData = indicatorsStorage.get(smaStorageKey);
		
		if ( indicatorData == null || !indicatorData.containsKey(new Integer(timePeriod)) )
			throw new RuntimeException("Не рассчитаны индикаторы " + indID + "[" + timePeriod + "] для тикера: " + ticker);
		else {
			List<Object> data = indicatorData.get(timePeriod);
			
			if (data == null)
				throw new RuntimeException("Не рассчитаны индикаторы " + indID + "[" + timePeriod + "] для тикера: " + ticker);
			else
				return data;
		}
	}
	
	public double findIndicatorValue(String ticker, int timePeriod, LocalDateTime date, String indID) {
		String indStorageKey = generateKeyForIndTicker(ticker, timePeriod, indID);

		
		HashMap<Integer, List<Object>> indicatorData = indicatorsStorage.get(indStorageKey);
		
		if ( indicatorData == null )
			throw new RuntimeException("Не рассчитаны индикаторы " + indID + "[" + timePeriod + "] для тикера: " + ticker + " на дату: " + date);
		else {
			List<Object> data = indicatorData.get(new Integer(timePeriod));
			
			if (data == null)
				throw new RuntimeException("Не рассчитаны индикаторы " + indID + "[" + timePeriod + "] для тикера: " + ticker + " на дату: " + date);
			else
				for (Object indData : data) {
					LocalDateTime dateObj = null;
					
					if ( indData instanceof IndicatorData)
						dateObj = ((IndicatorData) indData).getDateTime();
					else if (indData instanceof BBANDSData  ) 
						dateObj = ((BBANDSData) indData).getDateTime();
					
					if ( DateUtils.compareDatesByDay(dateObj, date) )
						if ( indData instanceof BBANDSData )
							return ((BBANDSData) indData).getLowerBand();
						else return ((IndicatorData)indData).getData();
					
				}
					
			throw new RuntimeException("Не рассчитаны индикаторы " + indID + "[" + timePeriod + "] для тикера: " + ticker + " на дату: " + date);
		}
	}
}
